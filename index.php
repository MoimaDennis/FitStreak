<?php
// FitStreak REST API - single-file PHP + MySQL (PDO), JWT (HS256) auth, bcrypt passwords
declare(strict_types=1);
header('Content-Type: application/json; charset=utf-8');
ini_set('display_errors', '0');
date_default_timezone_set('Africa/Johannesburg');

const DB_HOST = 'localhost';
const DB_NAME = 'fitstreak';
const DB_USER = 'root';
const DB_PASS = '';
const JWT_SECRET = 'fitstreak-change-this-secret';

function out($data, int $code = 200): void { http_response_code($code); echo json_encode($data); exit; }
function fail(string $msg, int $code = 400): void { out(['error' => $msg], $code); }
function b64e(string $s): string { return rtrim(strtr(base64_encode($s), '+/', '-_'), '='); }
function b64d(string $s): string { return base64_decode(strtr($s, '-_', '+/')); }

function jwt_make(int $uid): string {
    $h = b64e(json_encode(['alg' => 'HS256', 'typ' => 'JWT']));
    $p = b64e(json_encode(['user_id' => $uid, 'exp' => time() + 7 * 86400]));
    return "$h.$p." . b64e(hash_hmac('sha256', "$h.$p", JWT_SECRET, true));
}
function auth_user_id(): int {
    $h = $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? '';
    if (!$h && function_exists('getallheaders')) {
        foreach (getallheaders() as $k => $v) { if (strtolower($k) === 'authorization') $h = $v; }
    }
    $t = trim(preg_replace('/^Bearer\s+/i', '', $h));
    $p = explode('.', $t);
    if (count($p) !== 3) fail('Unauthorized', 401);
    $sig = b64e(hash_hmac('sha256', "$p[0].$p[1]", JWT_SECRET, true));
    if (!hash_equals($sig, $p[2])) fail('Unauthorized', 401);
    $pl = json_decode(b64d($p[1]), true);
    if (!$pl || ($pl['exp'] ?? 0) < time()) fail('Token expired', 401);
    return (int)$pl['user_id'];
}

try {
    $pdo = new PDO('mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=utf8mb4', DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
} catch (Throwable $e) { fail('Database unavailable', 500); }

function user_row(PDO $pdo, int $id) {
    $s = $pdo->prepare('SELECT user_id,username,email,streak_count,longest_streak,xp_total,level,units,notifications_enabled FROM users WHERE user_id=?');
    $s->execute([$id]);
    return $s->fetch();
}

$method = $_SERVER['REQUEST_METHOD'];
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$base = rtrim(str_replace('\\', '/', dirname($_SERVER['SCRIPT_NAME'])), '/');
$path = '/' . trim(substr($uri, strlen($base)), '/');
$in = json_decode(file_get_contents('php://input'), true) ?: [];

try {
    // ---- REGISTER ----
    if ($method === 'POST' && $path === '/api/register') {
        $u = trim((string)($in['username'] ?? '')); $e = trim((string)($in['email'] ?? '')); $pw = (string)($in['password'] ?? '');
        if ($u === '' || $e === '' || strlen($pw) < 6) fail('Missing fields or password under 6 characters');
        if (!filter_var($e, FILTER_VALIDATE_EMAIL)) fail('Invalid email');
        $c = $pdo->prepare('SELECT 1 FROM users WHERE email=? OR username=?'); $c->execute([$e, $u]);
        if ($c->fetch()) fail('Username or email already exists', 409);
        $pdo->prepare('INSERT INTO users (username,email,password_hash) VALUES (?,?,?)')
            ->execute([$u, $e, password_hash($pw, PASSWORD_BCRYPT)]);
        $id = (int)$pdo->lastInsertId();
        out(['user_id' => $id, 'user' => user_row($pdo, $id), 'token' => jwt_make($id)], 201);
    }
    // ---- LOGIN ----
    if ($method === 'POST' && $path === '/api/login') {
        $e = trim((string)($in['email'] ?? '')); $pw = (string)($in['password'] ?? '');
        $s = $pdo->prepare('SELECT user_id,password_hash FROM users WHERE email=?'); $s->execute([$e]);
        $r = $s->fetch();
        if (!$r || !password_verify($pw, $r['password_hash'])) fail('Invalid email or password', 401);
        $id = (int)$r['user_id'];
        out(['user_id' => $id, 'user' => user_row($pdo, $id), 'token' => jwt_make($id)]);
    }
    // ---- WORKOUTS (filters) ----
    if ($method === 'GET' && $path === '/api/workouts') {
        $w = []; $a = [];
        foreach (['body_part', 'intensity', 'equipment'] as $f) {
            $v = trim((string)($_GET[$f] ?? ''));
            if ($v !== '' && strtolower($v) !== 'all') { $w[] = "LOWER($f)=LOWER(?)"; $a[] = $v; }
        }
        $sql = 'SELECT workout_id,name,duration_minutes,intensity,body_part,equipment,video_url FROM workouts'
             . ($w ? ' WHERE ' . implode(' AND ', $w) : '') . ' ORDER BY workout_id';
        $s = $pdo->prepare($sql); $s->execute($a);
        out($s->fetchAll());
    }
    // ---- LOG WORKOUT (streak, XP, level, badges) ----
    if ($method === 'POST' && $path === '/api/workout-logs') {
        $uid = auth_user_id();
        $wid = (int)($in['workout_id'] ?? 0); $dur = (int)($in['duration_seconds'] ?? 0);
        $diff = (string)($in['difficulty_rating'] ?? 'Medium');
        $c = $pdo->prepare('SELECT 1 FROM workouts WHERE workout_id=?'); $c->execute([$wid]);
        if (!$c->fetch()) fail('Workout not found', 404);
        $mult = ['easy' => 1.0, 'medium' => 1.5, 'hard' => 2.0][strtolower($diff)] ?? 1.5;
        $xp = (int)round(max(10, ($dur / 60) * 10 * $mult));
        $s = $pdo->prepare('SELECT streak_count,longest_streak,xp_total,last_workout_date FROM users WHERE user_id=?');
        $s->execute([$uid]); $u = $s->fetch();
        if (!$u) fail('User not found', 404);
        $today = date('Y-m-d'); $yest = date('Y-m-d', strtotime('-1 day'));
        $streak = (int)$u['streak_count'];
        if ($u['last_workout_date'] !== $today) { $streak = ($u['last_workout_date'] === $yest) ? $streak + 1 : 1; }
        $longest = max($streak, (int)$u['longest_streak']);
        $xpTotal = (int)$u['xp_total'] + $xp;
        $level = intdiv($xpTotal, 100) + 1;
        $pdo->prepare('UPDATE users SET streak_count=?,longest_streak=?,xp_total=?,level=?,last_workout_date=? WHERE user_id=?')
            ->execute([$streak, $longest, $xpTotal, $level, $today, $uid]);
        $pdo->prepare('INSERT INTO workout_logs (user_id,workout_id,duration_seconds,difficulty_rating,xp_gained) VALUES (?,?,?,?,?)')
            ->execute([$uid, $wid, $dur, $diff, $xp]);
        $earn = ['First Workout'];
        if ($streak >= 3) $earn[] = '3-Day Streak';
        if ($streak >= 7) $earn[] = '7-Day Streak';
        if ($xpTotal >= 500) $earn[] = 'XP 500';
        $new = [];
        $ins = $pdo->prepare('INSERT IGNORE INTO user_badges (user_id,badge_id) SELECT ?,badge_id FROM badges WHERE badge_name=?');
        foreach ($earn as $name) { $ins->execute([$uid, $name]); if ($ins->rowCount() === 1) $new[] = $name; }
        out(['streak_count' => $streak, 'xp_gained' => (float)$xp, 'xp_total' => (float)$xpTotal, 'level' => $level, 'new_badges' => $new]);
    }
    // ---- BADGES ----
    if ($method === 'GET' && preg_match('#^/api/badges/(\d+)$#', $path, $m)) {
        $s = $pdo->prepare('SELECT b.badge_name,b.criteria,ub.earned_at FROM user_badges ub JOIN badges b ON b.badge_id=ub.badge_id WHERE ub.user_id=? ORDER BY ub.earned_at');
        $s->execute([(int)$m[1]]);
        out($s->fetchAll());
    }
    // ---- DAILY CHALLENGE ----
    if ($method === 'GET' && $path === '/api/challenges/today') {
        $n = (int)$pdo->query('SELECT COUNT(*) FROM challenges')->fetchColumn();
        if ($n === 0) fail('No challenges', 404);
        $i = ((int)date('z')) % $n;
        $r = $pdo->query("SELECT challenge_id,description FROM challenges ORDER BY challenge_id LIMIT 1 OFFSET $i")->fetch();
        out(['challenge_id' => (int)$r['challenge_id'], 'description' => $r['description'], 'active_date' => date('Y-m-d')]);
    }
    // ---- LEADERBOARD ----
    if ($method === 'GET' && preg_match('#^/api/challenges/(\d+)/leaderboard$#', $path, $m)) {
        $s = $pdo->prepare('SELECT u.username,r.result FROM challenge_results r JOIN users u ON u.user_id=r.user_id WHERE r.challenge_id=? ORDER BY r.result DESC LIMIT 10');
        $s->execute([(int)$m[1]]);
        out($s->fetchAll());
    }
    // ---- SUBMIT CHALLENGE RESULT ----
    if ($method === 'POST' && preg_match('#^/api/challenges/(\d+)/submit$#', $path, $m)) {
        $uid = auth_user_id();
        $val = (int)(reset($in) ?: 0);
        $pdo->prepare('INSERT INTO challenge_results (challenge_id,user_id,result) VALUES (?,?,?) ON DUPLICATE KEY UPDATE result=GREATEST(result,VALUES(result))')
            ->execute([(int)$m[1], $uid, $val]);
        out(['message' => 'Result submitted']);
    }
    // ---- UPDATE SETTINGS ----
    if ($method === 'PUT' && $path === '/api/profile') {
        $uid = auth_user_id();
        $units = (string)($in['units'] ?? 'metric'); $n = (int)($in['notifications_enabled'] ?? 1);
        $pdo->prepare('UPDATE users SET units=?,notifications_enabled=? WHERE user_id=?')->execute([$units, $n, $uid]);
        out(['message' => 'Profile updated']);
    }
    fail('Not found', 404);
} catch (Throwable $e) {
    fail('Server error', 500);
}