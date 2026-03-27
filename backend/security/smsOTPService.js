const axios = require("axios");

const otpStore = new Map();

function generateOTP(phone){

    const otp = Math.floor(100000 + Math.random() * 900000).toString();

    otpStore.set(phone,{
        otp,
        expires: Date.now() + 300000
    });

    return otp;
}

async function sendSMSOTP(phone){

    const otp = generateOTP(phone);

    const message = `ConsentLens Parent Verification OTP: ${otp}`;

    await axios.post("https://www.fast2sms.com/dev/bulkV2", {
        route: "q",
        message: message,
        language: "english",
        flash: 0,
        numbers: phone
    },{
        headers:{
            authorization: process.env.FAST2SMS_API_KEY,
            "Content-Type":"application/json"
        }
    });

}

function verifySMSOTP(phone,enteredOTP){

    const record = otpStore.get(phone);

    if(!record) return false;

    if(record.expires < Date.now()){
        otpStore.delete(phone);
        return false;
    }

    if(record.otp === enteredOTP){
        otpStore.delete(phone);
        return true;
    }

    return false;
}

module.exports = { sendSMSOTP, verifySMSOTP };