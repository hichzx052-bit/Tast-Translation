<?php
/*
 * Hichamdzz AI Studio Bot 🎨🎬
 * Token: 8444095447:AAEVn6os9pPwGonoE1i8N2yXDKauNu9CFhU
 * توليد صور + فيديو + تحويل صورة لفيديو
 * By: هشوم 🪶 لبابا هشام ❤️
 */

$BOT_TOKEN = "8444095447:AAEVn6os9pPwGonoE1i8N2yXDKauNu9CFhU";
$OWNER_ID = 7581079032;
$VIP_IDS = [8074396666]; // عنتر
$ADMIN_PASSWORD = "HACHOMPI";
$SETTINGS_PASSWORD = "Hichamdzz";
$DB_FILE = __DIR__ . "/bot_db.json";

// ========== DATABASE ==========
function loadDB() {
    global $DB_FILE;
    if (file_exists($DB_FILE)) {
        return json_decode(file_get_contents($DB_FILE), true) ?: [];
    }
    return ["users" => [], "stats" => ["images" => 0, "videos" => 0, "i2v" => 0], "banned" => [], "states" => []];
}

function saveDB($db) {
    global $DB_FILE;
    file_put_contents($DB_FILE, json_encode($db, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
}

// ========== TELEGRAM API ==========
function tg($method, $params = []) {
    global $BOT_TOKEN;
    $url = "https://api.telegram.org/bot$BOT_TOKEN/$method";
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 120);
    curl_setopt($ch, CURLOPT_POST, true);
    
    // Check if we have file uploads
    $hasFile = false;
    foreach ($params as $v) {
        if ($v instanceof CURLFile) { $hasFile = true; break; }
    }
    
    if ($hasFile) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, $params);
    } else {
        curl_setopt($ch, CURLOPT_HTTPHEADER, ["Content-Type: application/json"]);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($params));
    }
    
    $res = curl_exec($ch);
    curl_close($ch);
    return json_decode($res, true);
}

function sendMsg($chatId, $text, $buttons = null, $parseMode = "HTML") {
    $params = ["chat_id" => $chatId, "text" => $text, "parse_mode" => $parseMode];
    if ($buttons) {
        $params["reply_markup"] = json_encode(["inline_keyboard" => $buttons]);
    }
    return tg("sendMessage", $params);
}

function sendPhoto($chatId, $url, $caption = "") {
    return tg("sendPhoto", [
        "chat_id" => $chatId,
        "photo" => $url,
        "caption" => $caption,
        "parse_mode" => "HTML"
    ]);
}

function sendVideo($chatId, $filePath, $caption = "") {
    return tg("sendVideo", [
        "chat_id" => $chatId,
        "video" => new CURLFile($filePath, "video/mp4"),
        "caption" => $caption,
        "parse_mode" => "HTML",
        "supports_streaming" => true
    ]);
}

function sendAction($chatId, $action = "typing") {
    tg("sendChatAction", ["chat_id" => $chatId, "action" => $action]);
}

function editMsg($chatId, $msgId, $text, $buttons = null) {
    $params = ["chat_id" => $chatId, "message_id" => $msgId, "text" => $text, "parse_mode" => "HTML"];
    if ($buttons) {
        $params["reply_markup"] = json_encode(["inline_keyboard" => $buttons]);
    }
    return tg("editMessageText", $params);
}

function answerCallback($callbackId, $text = "") {
    tg("answerCallbackQuery", ["callback_query_id" => $callbackId, "text" => $text]);
}

// ========== IMAGE GENERATION ==========
function generateImage($prompt, $style = "none", $width = 1024, $height = 1024) {
    $stylePrompts = [
        "anime" => "anime style, vibrant colors, detailed, ",
        "realistic" => "ultra realistic, photographic, 8k, detailed, ",
        "fantasy" => "fantasy art, magical, epic, detailed, ",
        "cyberpunk" => "cyberpunk style, neon lights, futuristic, ",
        "3d" => "3D render, octane render, detailed, ",
        "cartoon" => "cartoon style, colorful, fun, ",
        "oil" => "oil painting style, masterpiece, detailed brushstrokes, ",
        "watercolor" => "watercolor painting, soft colors, artistic, ",
        "pixel" => "pixel art, retro, 8-bit style, ",
        "none" => ""
    ];
    
    $fullPrompt = ($stylePrompts[$style] ?? "") . $prompt;
    $encodedPrompt = urlencode($fullPrompt);
    $seed = rand(1, 999999);
    
    $url = "https://image.pollinations.ai/prompt/{$encodedPrompt}?width={$width}&height={$height}&nologo=true&seed={$seed}&model=flux";
    
    return $url;
}

// ========== VIDEO GENERATION (HuggingFace Wan2.1) ==========
function submitVideoJob($prompt, $size = "720*1280") {
    $url = "https://wan-ai-wan2-1.hf.space/gradio_api/call/t2v_generation_async";
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ["Content-Type: application/json"]);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
        "data" => [$prompt, $size, false, -1]
    ]));
    $res = curl_exec($ch);
    curl_close($ch);
    $data = json_decode($res, true);
    return $data["event_id"] ?? null;
}

function submitI2VJob($prompt, $imageUrl) {
    // Download image first
    $imgData = file_get_contents($imageUrl);
    if (!$imgData) return null;
    
    $tmpFile = tempnam(sys_get_temp_dir(), "i2v_") . ".jpg";
    file_put_contents($tmpFile, $imgData);
    
    // Upload to HF Space
    $uploadUrl = "https://wan-ai-wan2-1.hf.space/gradio_api/upload";
    $ch = curl_init($uploadUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, [
        "files" => new CURLFile($tmpFile, "image/jpeg", "image.jpg")
    ]);
    $uploadRes = curl_exec($ch);
    curl_close($ch);
    @unlink($tmpFile);
    
    $uploadData = json_decode($uploadRes, true);
    if (!$uploadData || empty($uploadData)) return null;
    
    $filePath = $uploadData[0];
    
    // Submit i2v job
    $url = "https://wan-ai-wan2-1.hf.space/gradio_api/call/i2v_generation_async";
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ["Content-Type: application/json"]);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
        "data" => [
            $prompt,
            ["path" => $filePath],
            false,
            -1
        ]
    ]));
    $res = curl_exec($ch);
    curl_close($ch);
    $data = json_decode($res, true);
    return $data["event_id"] ?? null;
}

function pollVideoResult($eventId, $endpoint = "t2v_generation_async") {
    $url = "https://wan-ai-wan2-1.hf.space/gradio_api/call/{$endpoint}/{$eventId}";
    
    $maxWait = 90; // seconds
    $start = time();
    
    while (time() - $start < $maxWait) {
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_TIMEOUT, 15);
        $res = curl_exec($ch);
        curl_close($ch);
        
        if (!$res) { sleep(3); continue; }
        
        // Parse SSE response
        $lines = explode("\n", $res);
        foreach ($lines as $line) {
            if (strpos($line, "data: ") === 0) {
                $jsonStr = substr($line, 6);
                $data = json_decode($jsonStr, true);
                if (is_array($data)) {
                    // Look for video URL in response
                    foreach ($data as $item) {
                        if (is_array($item) && isset($item["video"])) {
                            return $item["video"]["url"] ?? $item["video"]["path"] ?? null;
                        }
                        if (is_array($item) && isset($item["url"])) {
                            return $item["url"];
                        }
                        if (is_array($item) && isset($item["path"])) {
                            $path = $item["path"];
                            if (strpos($path, "http") === 0) return $path;
                            return "https://wan-ai-wan2-1.hf.space/gradio_api/file=" . $path;
                        }
                    }
                    // Maybe first element is the video
                    if (isset($data[0]) && is_string($data[0]) && strpos($data[0], ".mp4") !== false) {
                        $path = $data[0];
                        if (strpos($path, "http") === 0) return $path;
                        return "https://wan-ai-wan2-1.hf.space/gradio_api/file=" . $path;
                    }
                }
            }
            // Check for complete event
            if (strpos($line, "event: complete") !== false) {
                // Next data line has the result
                continue;
            }
            if (strpos($line, "event: error") !== false) {
                return "ERROR";
            }
        }
        
        sleep(5);
    }
    
    return null; // timeout
}

function downloadFile($url) {
    $tmpFile = tempnam(sys_get_temp_dir(), "vid_") . ".mp4";
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 60);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    $data = curl_exec($ch);
    curl_close($ch);
    
    if ($data && strlen($data) > 1000) {
        file_put_contents($tmpFile, $data);
        return $tmpFile;
    }
    return null;
}

// ========== PERMISSIONS ==========
function isOwner($userId) {
    global $OWNER_ID;
    return $userId == $OWNER_ID;
}

function isVIP($userId) {
    global $VIP_IDS;
    return in_array($userId, $VIP_IDS);
}

function isBanned($userId, $db) {
    return in_array($userId, $db["banned"] ?? []);
}

function getUserState($userId, $db) {
    return $db["states"][$userId] ?? null;
}

function setUserState($userId, $state, &$db) {
    $db["states"][$userId] = $state;
    saveDB($db);
}

function clearUserState($userId, &$db) {
    unset($db["states"][$userId]);
    saveDB($db);
}

// ========== STYLE BUTTONS ==========
function getStyleButtons($prefix = "style") {
    return [
        [
            ["text" => "🎌 أنمي", "callback_data" => "{$prefix}_anime"],
            ["text" => "📷 واقعي", "callback_data" => "{$prefix}_realistic"]
        ],
        [
            ["text" => "🧙 فانتازي", "callback_data" => "{$prefix}_fantasy"],
            ["text" => "🌃 سايبربنك", "callback_data" => "{$prefix}_cyberpunk"]
        ],
        [
            ["text" => "🎮 3D", "callback_data" => "{$prefix}_3d"],
            ["text" => "🎨 كرتوني", "callback_data" => "{$prefix}_cartoon"]
        ],
        [
            ["text" => "🖼 زيتي", "callback_data" => "{$prefix}_oil"],
            ["text" => "💧 ألوان مائية", "callback_data" => "{$prefix}_watercolor"]
        ],
        [
            ["text" => "👾 بكسل", "callback_data" => "{$prefix}_pixel"],
            ["text" => "✨ بدون ستايل", "callback_data" => "{$prefix}_none"]
        ]
    ];
}

// ========== MAIN KEYBOARD ==========
function getMainKeyboard() {
    return json_encode([
        "keyboard" => [
            [["text" => "🖼 توليد صورة"], ["text" => "🎬 توليد فيديو"]],
            [["text" => "🖼➡🎬 صورة لفيديو"], ["text" => "ℹ️ معلومات"]]
        ],
        "resize_keyboard" => true,
        "is_persistent" => true
    ]);
}

// ========== PROCESS UPDATE ==========
$input = file_get_contents("php://input");
$update = json_decode($input, true);

if (!$update) exit;

$db = loadDB();

// Handle callback queries
if (isset($update["callback_query"])) {
    $cb = $update["callback_query"];
    $cbId = $cb["id"];
    $chatId = $cb["message"]["chat"]["id"];
    $msgId = $cb["message"]["message_id"];
    $userId = $cb["from"]["id"];
    $cbData = $cb["data"];
    
    if (isBanned($userId, $db)) {
        answerCallback($cbId, "⛔ محظور");
        exit;
    }
    
    // Style selection for image
    if (strpos($cbData, "style_") === 0) {
        $style = str_replace("style_", "", $cbData);
        $state = getUserState($userId, $db);
        
        if ($state && $state["action"] === "gen_image") {
            $prompt = $state["prompt"];
            answerCallback($cbId, "🎨 جاري التوليد...");
            editMsg($chatId, $msgId, "⏳ <b>جاري توليد الصورة...</b>\n\n📝 {$prompt}\n🎨 ستايل: {$style}");
            sendAction($chatId, "upload_photo");
            
            $imgUrl = generateImage($prompt, $style);
            
            $result = sendPhoto($chatId, $imgUrl, "🖼 <b>صورتك جاهزة!</b>\n\n📝 {$prompt}\n🎨 ستايل: {$style}\n\n🔄 أرسل وصف جديد لتوليد صورة أخرى");
            
            if (!$result || !($result["ok"] ?? false)) {
                // Retry with direct download
                $imgData = file_get_contents($imgUrl);
                if ($imgData) {
                    $tmpFile = tempnam(sys_get_temp_dir(), "img_") . ".jpg";
                    file_put_contents($tmpFile, $imgData);
                    tg("sendPhoto", [
                        "chat_id" => $chatId,
                        "photo" => new CURLFile($tmpFile, "image/jpeg"),
                        "caption" => "🖼 <b>صورتك جاهزة!</b>\n\n📝 {$prompt}\n🎨 ستايل: {$style}",
                        "parse_mode" => "HTML"
                    ]);
                    @unlink($tmpFile);
                } else {
                    sendMsg($chatId, "❌ فشل التوليد. جرب مرة ثانية.");
                }
            }
            
            // Regenerate button
            sendMsg($chatId, "🔄 تبي نفس الوصف بستايل ثاني؟", [
                [["text" => "🔄 إعادة التوليد", "callback_data" => "regen_img"]],
                [["text" => "🏠 القائمة الرئيسية", "callback_data" => "main_menu"]]
            ]);
            
            $db["stats"]["images"]++;
            saveDB($db);
            clearUserState($userId, $db);
        }
        exit;
    }
    
    // Video size selection
    if (strpos($cbData, "vsize_") === 0) {
        $size = str_replace("vsize_", "", $cbData);
        $state = getUserState($userId, $db);
        
        if ($state && $state["action"] === "gen_video") {
            $prompt = $state["prompt"];
            answerCallback($cbId, "🎬 جاري التوليد...");
            
            $statusMsg = editMsg($chatId, $msgId, "⏳ <b>جاري توليد الفيديو...</b>\n\n📝 {$prompt}\n📐 {$size}\n\n⚠️ قد يستغرق 1-3 دقائق. انتظر...");
            sendAction($chatId, "upload_video");
            
            $eventId = submitVideoJob($prompt, $size);
            
            if (!$eventId) {
                editMsg($chatId, $msgId, "❌ <b>فشل الاتصال بسيرفر الفيديو</b>\n\nالسيرفر مشغول. جرب بعد دقيقة.");
                clearUserState($userId, $db);
                exit;
            }
            
            // Poll for result
            editMsg($chatId, $msgId, "⏳ <b>الفيديو قيد التوليد...</b>\n\n📝 {$prompt}\n🆔 Job: {$eventId}\n\n⏱ انتظر 1-3 دقائق...");
            
            $videoUrl = pollVideoResult($eventId, "t2v_generation_async");
            
            if ($videoUrl && $videoUrl !== "ERROR") {
                sendAction($chatId, "upload_video");
                $tmpVideo = downloadFile($videoUrl);
                
                if ($tmpVideo) {
                    sendVideo($chatId, $tmpVideo, "🎬 <b>فيديوك جاهز!</b>\n\n📝 {$prompt}");
                    @unlink($tmpVideo);
                    $db["stats"]["videos"]++;
                    saveDB($db);
                    editMsg($chatId, $msgId, "✅ <b>تم توليد الفيديو بنجاح!</b>");
                } else {
                    editMsg($chatId, $msgId, "❌ <b>فشل تحميل الفيديو</b>\n\nجرب مرة ثانية.");
                }
            } else {
                editMsg($chatId, $msgId, "❌ <b>فشل توليد الفيديو</b>\n\nالسيرفر مشغول أو الطلب كبير. جرب:\n• وصف أقصر\n• وقت ثاني");
            }
            
            clearUserState($userId, $db);
        }
        exit;
    }
    
    // Regen image
    if ($cbData === "regen_img") {
        $state = getUserState($userId, $db);
        if ($state && isset($state["prompt"])) {
            setUserState($userId, ["action" => "gen_image", "prompt" => $state["prompt"]], $db);
            editMsg($chatId, $msgId, "🎨 <b>اختر الستايل:</b>\n\n📝 {$state['prompt']}", getStyleButtons());
        } else {
            answerCallback($cbId, "أرسل وصف جديد");
        }
        exit;
    }
    
    // Main menu
    if ($cbData === "main_menu") {
        answerCallback($cbId, "🏠");
        editMsg($chatId, $msgId, "🏠 <b>القائمة الرئيسية</b>\n\nاختر من الأزرار بالأسفل 👇");
        clearUserState($userId, $db);
        exit;
    }
    
    // Admin panel
    if ($cbData === "admin_panel") {
        if (!isOwner($userId)) {
            answerCallback($cbId, "⛔ للمالك فقط");
            exit;
        }
        $userCount = count($db["users"] ?? []);
        $imgCount = $db["stats"]["images"] ?? 0;
        $vidCount = $db["stats"]["videos"] ?? 0;
        $i2vCount = $db["stats"]["i2v"] ?? 0;
        $bannedCount = count($db["banned"] ?? []);
        
        editMsg($chatId, $msgId, 
            "🔧 <b>لوحة الأدمن</b>\n\n" .
            "👥 المستخدمين: <b>{$userCount}</b>\n" .
            "🖼 صور مولّدة: <b>{$imgCount}</b>\n" .
            "🎬 فيديوهات: <b>{$vidCount}</b>\n" .
            "🖼➡🎬 صورة→فيديو: <b>{$i2vCount}</b>\n" .
            "⛔ محظورين: <b>{$bannedCount}</b>",
            [
                [["text" => "📋 قائمة المستخدمين", "callback_data" => "admin_users"]],
                [["text" => "📢 إرسال للكل", "callback_data" => "admin_broadcast"]],
                [["text" => "🏠 رجوع", "callback_data" => "main_menu"]]
            ]
        );
        answerCallback($cbId);
        exit;
    }
    
    if ($cbData === "admin_users") {
        if (!isOwner($userId)) { answerCallback($cbId, "⛔"); exit; }
        $text = "👥 <b>المستخدمين:</b>\n\n";
        $users = $db["users"] ?? [];
        $i = 0;
        foreach ($users as $uid => $info) {
            $i++;
            if ($i > 30) { $text .= "\n... و" . (count($users) - 30) . " آخرين"; break; }
            $name = $info["name"] ?? "مجهول";
            $vip = in_array($uid, $GLOBALS["VIP_IDS"] ?? []) ? " ⭐VIP" : "";
            $banned = in_array($uid, $db["banned"] ?? []) ? " ⛔" : "";
            $text .= "{$i}. <code>{$uid}</code> — {$name}{$vip}{$banned}\n";
        }
        editMsg($chatId, $msgId, $text, [
            [["text" => "🔙 رجوع", "callback_data" => "admin_panel"]]
        ]);
        answerCallback($cbId);
        exit;
    }
    
    if ($cbData === "admin_broadcast") {
        if (!isOwner($userId)) { answerCallback($cbId, "⛔"); exit; }
        setUserState($userId, ["action" => "broadcast"], $db);
        editMsg($chatId, $msgId, "📢 <b>أرسل الرسالة اللي تبي توصل للكل:</b>");
        answerCallback($cbId);
        exit;
    }
    
    answerCallback($cbId);
    exit;
}

// Handle messages
if (!isset($update["message"])) exit;

$msg = $update["message"];
$chatId = $msg["chat"]["id"];
$userId = $msg["from"]["id"];
$userName = $msg["from"]["first_name"] ?? "مجهول";
$text = $msg["text"] ?? "";

// Register user
if (!isset($db["users"][$userId])) {
    $db["users"][$userId] = [
        "name" => $userName,
        "joined" => date("Y-m-d H:i"),
        "username" => $msg["from"]["username"] ?? null
    ];
    saveDB($db);
    
    // Notify owner
    if (!isOwner($userId)) {
        $uname = $msg["from"]["username"] ?? "بدون";
        tg("sendMessage", [
            "chat_id" => $OWNER_ID,
            "text" => "👤 <b>مستخدم جديد!</b>\n\nالاسم: {$userName}\nيوزرنيم: @{$uname}\nآيدي: <code>{$userId}</code>",
            "parse_mode" => "HTML"
        ]);
    }
}

// Check banned
if (isBanned($userId, $db)) {
    sendMsg($chatId, "⛔ أنت محظور من استخدام البوت.");
    exit;
}

// Handle states
$state = getUserState($userId, $db);

// ===== BROADCAST STATE =====
if ($state && $state["action"] === "broadcast" && isOwner($userId)) {
    $sent = 0;
    $failed = 0;
    foreach ($db["users"] as $uid => $info) {
        $result = tg("sendMessage", ["chat_id" => $uid, "text" => $text, "parse_mode" => "HTML"]);
        if ($result && ($result["ok"] ?? false)) $sent++;
        else $failed++;
        usleep(100000); // 100ms delay
    }
    sendMsg($chatId, "📢 <b>تم الإرسال!</b>\n\n✅ نجح: {$sent}\n❌ فشل: {$failed}");
    clearUserState($userId, $db);
    exit;
}

// ===== IMAGE PROMPT STATE =====
if ($state && $state["action"] === "waiting_image_prompt") {
    if ($text) {
        setUserState($userId, ["action" => "gen_image", "prompt" => $text], $db);
        sendMsg($chatId, "🎨 <b>اختر الستايل للصورة:</b>\n\n📝 <i>{$text}</i>", getStyleButtons());
    } else {
        sendMsg($chatId, "❌ أرسل وصف نصي للصورة");
    }
    exit;
}

// ===== VIDEO PROMPT STATE =====
if ($state && $state["action"] === "waiting_video_prompt") {
    if ($text) {
        setUserState($userId, ["action" => "gen_video", "prompt" => $text], $db);
        sendMsg($chatId, "📐 <b>اختر حجم الفيديو:</b>\n\n📝 <i>{$text}</i>", [
            [
                ["text" => "📱 عمودي (720×1280)", "callback_data" => "vsize_720*1280"],
                ["text" => "🖥 أفقي (1280×720)", "callback_data" => "vsize_1280*720"]
            ],
            [
                ["text" => "⬜ مربع (960×960)", "callback_data" => "vsize_960*960"]
            ]
        ]);
    } else {
        sendMsg($chatId, "❌ أرسل وصف نصي للفيديو");
    }
    exit;
}

// ===== I2V STATE (waiting for image) =====
if ($state && $state["action"] === "waiting_i2v_image") {
    $photo = $msg["photo"] ?? null;
    if ($photo) {
        $fileId = end($photo)["file_id"];
        $fileInfo = tg("getFile", ["file_id" => $fileId]);
        $filePath = $fileInfo["result"]["file_path"] ?? null;
        
        if ($filePath) {
            $imageUrl = "https://api.telegram.org/file/bot{$GLOBALS['BOT_TOKEN']}/{$filePath}";
            setUserState($userId, ["action" => "waiting_i2v_prompt", "image_url" => $imageUrl], $db);
            sendMsg($chatId, "✅ <b>الصورة وصلت!</b>\n\nالحين أرسل وصف الحركة اللي تبيها بالفيديو:\n\n<i>مثال: الشخص يمشي للأمام ببطء</i>");
        } else {
            sendMsg($chatId, "❌ فشل تحميل الصورة. جرب مرة ثانية.");
        }
    } else {
        sendMsg($chatId, "❌ أرسل صورة (مو نص)");
    }
    exit;
}

// ===== I2V PROMPT STATE =====
if ($state && $state["action"] === "waiting_i2v_prompt") {
    if ($text) {
        $imageUrl = $state["image_url"];
        $statusMsg = sendMsg($chatId, "⏳ <b>جاري تحويل الصورة لفيديو...</b>\n\n📝 {$text}\n\n⚠️ قد يستغرق 1-3 دقائق...");
        sendAction($chatId, "upload_video");
        
        $eventId = submitI2VJob($text, $imageUrl);
        
        if (!$eventId) {
            editMsg($chatId, $statusMsg["result"]["message_id"], "❌ <b>فشل الاتصال بسيرفر الفيديو</b>\n\nجرب مرة ثانية بعد دقيقة.");
            clearUserState($userId, $db);
            exit;
        }
        
        editMsg($chatId, $statusMsg["result"]["message_id"], "⏳ <b>الفيديو قيد التوليد...</b>\n\n🆔 Job: {$eventId}\n⏱ انتظر...");
        
        $videoUrl = pollVideoResult($eventId, "i2v_generation_async");
        
        if ($videoUrl && $videoUrl !== "ERROR") {
            sendAction($chatId, "upload_video");
            $tmpVideo = downloadFile($videoUrl);
            
            if ($tmpVideo) {
                sendVideo($chatId, $tmpVideo, "🎬 <b>الفيديو جاهز!</b>\n\n📝 {$text}");
                @unlink($tmpVideo);
                $db["stats"]["i2v"]++;
                saveDB($db);
                editMsg($chatId, $statusMsg["result"]["message_id"], "✅ <b>تم!</b>");
            } else {
                editMsg($chatId, $statusMsg["result"]["message_id"], "❌ فشل تحميل الفيديو.");
            }
        } else {
            editMsg($chatId, $statusMsg["result"]["message_id"], "❌ <b>فشل توليد الفيديو</b>\n\nالسيرفر مشغول. جرب بعد شوي.");
        }
        
        clearUserState($userId, $db);
    } else {
        sendMsg($chatId, "❌ أرسل وصف نصي للحركة");
    }
    exit;
}

// ========== COMMANDS ==========
if ($text === "/start") {
    $welcome = isOwner($userId) 
        ? "مرحبا بابا هشام! ❤️🪶\n\n"
        : (isVIP($userId) 
            ? "مرحبا بك يا VIP! ⭐\nأنت ضيف مميز عند هشام 🎉\n\n"
            : "مرحبا بك في <b>Hichamdzz AI Studio</b>! 🎨🎬\n\n");
    
    $welcome .= "🖼 <b>توليد صور</b> — صوّر أي شي من وصف\n";
    $welcome .= "🎬 <b>توليد فيديو</b> — فيديو AI من وصف\n";
    $welcome .= "🖼➡🎬 <b>صورة لفيديو</b> — حوّل صورتك لفيديو متحرك\n\n";
    $welcome .= "اختر من الأزرار بالأسفل 👇";
    
    tg("sendMessage", [
        "chat_id" => $chatId,
        "text" => $welcome,
        "parse_mode" => "HTML",
        "reply_markup" => getMainKeyboard()
    ]);
    exit;
}

if ($text === "/admin" || $text === "🔧 أدمن") {
    if (isOwner($userId)) {
        sendMsg($chatId, "🔧 <b>لوحة الأدمن</b>", [
            [["text" => "📊 الإحصائيات", "callback_data" => "admin_panel"]],
            [["text" => "📋 المستخدمين", "callback_data" => "admin_users"]],
            [["text" => "📢 إرسال للكل", "callback_data" => "admin_broadcast"]]
        ]);
    } else {
        sendMsg($chatId, "🔐 هذا الأمر للأدمن فقط يا صديقي 😊");
    }
    exit;
}

// ===== KEYBOARD BUTTONS =====
if ($text === "🖼 توليد صورة") {
    setUserState($userId, ["action" => "waiting_image_prompt"], $db);
    sendMsg($chatId, "🖼 <b>توليد صورة بالذكاء الاصطناعي</b>\n\n📝 أرسل وصف الصورة اللي تبيها:\n\n<i>مثال: محارب أسطوري يقف فوق جبل عند الغروب</i>");
    exit;
}

if ($text === "🎬 توليد فيديو") {
    setUserState($userId, ["action" => "waiting_video_prompt"], $db);
    sendMsg($chatId, "🎬 <b>توليد فيديو بالذكاء الاصطناعي</b>\n\n📝 أرسل وصف الفيديو اللي تبيه:\n\n<i>مثال: قطة تمشي في حديقة جميلة</i>\n\n⚠️ التوليد يأخذ 1-3 دقائق");
    exit;
}

if ($text === "🖼➡🎬 صورة لفيديو") {
    setUserState($userId, ["action" => "waiting_i2v_image"], $db);
    sendMsg($chatId, "🖼➡🎬 <b>تحويل صورة لفيديو</b>\n\n📸 أرسل الصورة اللي تبي تحوّلها لفيديو:");
    exit;
}

if ($text === "ℹ️ معلومات") {
    sendMsg($chatId, 
        "ℹ️ <b>Hichamdzz AI Studio</b>\n\n" .
        "🤖 بوت ذكاء اصطناعي لتوليد الصور والفيديوهات\n\n" .
        "📌 <b>الميزات:</b>\n" .
        "• 🖼 توليد صور من وصف (10 ستايلات)\n" .
        "• 🎬 توليد فيديو من وصف (Wan2.1 AI)\n" .
        "• 🖼➡🎬 تحويل صورة لفيديو متحرك\n\n" .
        "👨‍💻 <b>المطور:</b> Hichamdzz\n" .
        "🪶 <b>مساعد:</b> هشوم"
    );
    exit;
}

// ===== BAN/UNBAN (owner only) =====
if (strpos($text, "/ban ") === 0 && isOwner($userId)) {
    $targetId = (int) substr($text, 5);
    if ($targetId && !in_array($targetId, $db["banned"])) {
        $db["banned"][] = $targetId;
        saveDB($db);
        sendMsg($chatId, "⛔ تم حظر: <code>{$targetId}</code>");
    }
    exit;
}

if (strpos($text, "/unban ") === 0 && isOwner($userId)) {
    $targetId = (int) substr($text, 7);
    $db["banned"] = array_values(array_filter($db["banned"], fn($id) => $id != $targetId));
    saveDB($db);
    sendMsg($chatId, "✅ تم فك الحظر: <code>{$targetId}</code>");
    exit;
}

// ===== DEFAULT: treat as image prompt =====
if ($text && !$state) {
    // If just text, treat as image generation
    setUserState($userId, ["action" => "gen_image", "prompt" => $text], $db);
    sendMsg($chatId, "🎨 <b>اختر الستايل للصورة:</b>\n\n📝 <i>{$text}</i>", getStyleButtons());
    exit;
}

// Handle photo without state (auto i2v)
if (isset($msg["photo"]) && !$state) {
    $photo = $msg["photo"];
    $fileId = end($photo)["file_id"];
    $fileInfo = tg("getFile", ["file_id" => $fileId]);
    $filePath = $fileInfo["result"]["file_path"] ?? null;
    
    if ($filePath) {
        $imageUrl = "https://api.telegram.org/file/bot{$BOT_TOKEN}/{$filePath}";
        $caption = $msg["caption"] ?? null;
        
        if ($caption) {
            // Has caption = direct i2v
            $statusMsg = sendMsg($chatId, "⏳ <b>جاري تحويل الصورة لفيديو...</b>\n\n📝 {$caption}\n\n⚠️ قد يستغرق 1-3 دقائق...");
            sendAction($chatId, "upload_video");
            
            $eventId = submitI2VJob($caption, $imageUrl);
            if ($eventId) {
                $videoUrl = pollVideoResult($eventId, "i2v_generation_async");
                if ($videoUrl && $videoUrl !== "ERROR") {
                    $tmpVideo = downloadFile($videoUrl);
                    if ($tmpVideo) {
                        sendVideo($chatId, $tmpVideo, "🎬 <b>الفيديو جاهز!</b>\n\n📝 {$caption}");
                        @unlink($tmpVideo);
                        $db["stats"]["i2v"]++;
                        saveDB($db);
                    }
                }
            }
            if (!isset($tmpVideo) || !$tmpVideo) {
                editMsg($chatId, $statusMsg["result"]["message_id"], "❌ فشل التوليد. جرب مرة ثانية.");
            }
        } else {
            // No caption, ask for description
            setUserState($userId, ["action" => "waiting_i2v_prompt", "image_url" => $imageUrl], $db);
            sendMsg($chatId, "📸 <b>الصورة وصلت!</b>\n\nأرسل وصف الحركة:\n<i>مثال: الشخص يبتسم ويلوّح</i>");
        }
    }
    exit;
}
