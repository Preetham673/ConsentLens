const twilio = require("twilio");
const otpGenerator = require("otp-generator");

const accountSid = process.env.TWILIO_SID;
const authToken = process.env.TWILIO_TOKEN;

const client = new twilio(accountSid, authToken);

/* ===============================
   OTP TEMP STORAGE
================================ */

const otpStore = new Map();

/* ===============================
   SEND OTP
================================ */

async function sendOTP(phone){

    const otp = otpGenerator.generate(6,{
        upperCaseAlphabets:false,
        lowerCaseAlphabets:false,
        specialChars:false
    });

    otpStore.set(phone,{
        otp,
        expires: Date.now() + 5*60*1000
    });

    await client.messages.create({
        body:`ConsentLens Parent Verification OTP: ${otp}`,
        from: process.env.TWILIO_PHONE,
        to: phone
    });

}

/* ===============================
   VERIFY OTP
================================ */

function verifyOTP(phone,otp){

    const record = otpStore.get(phone);

    if(!record) return false;

    if(record.expires < Date.now()){
        otpStore.delete(phone);
        return false;
    }

    if(record.otp === otp){
        otpStore.delete(phone);
        return true;
    }

    return false;
}

console.log("Twilio SID:", process.env.TWILIO_SID);
console.log("Twilio Phone:", process.env.TWILIO_PHONE);

module.exports = {
    sendOTP,
    verifyOTP
};