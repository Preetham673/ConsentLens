// Run: node test_whatsapp.js
const { Client, LocalAuth } = require("whatsapp-web.js");
const qrcode = require("qrcode-terminal");

// ✅ CHANGE THIS to the parent phone number
// Country code + number, no + sign
// India: 91 + 10 digits = 919876543210
const TEST_PHONE = "917075312576";

const client = new Client({
    authStrategy: new LocalAuth(),
    puppeteer: {
        headless: true,
        args: ["--no-sandbox", "--disable-setuid-sandbox"]
    }
});

client.on("qr", (qr) => {
    console.log("\n📱 Scan this QR with YOUR WhatsApp (the sender number):");
    qrcode.generate(qr, { small: true });
});

client.on("authenticated", () => {
    console.log("🔐 Authenticated!");
});

client.on("ready", async () => {
    console.log("✅ WhatsApp ready! Sending test message...");
    try {
        await client.sendMessage(`${TEST_PHONE}@c.us`,
            "✅ ConsentLens OTP test - WhatsApp is working!");
        console.log("✅ Message sent! Check the phone now.");
    } catch (err) {
        console.error("❌ Failed:", err.message);
    }
    process.exit(0);
});

client.on("auth_failure", () => {
    console.error("❌ Auth failed - delete .wwebjs_auth folder and retry");
    process.exit(1);
});

client.initialize();