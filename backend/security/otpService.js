const otpStore = new Map();

function generateOTP(parentPhone) {

    const otp = Math.floor(100000 + Math.random() * 900000).toString();

    otpStore.set(parentPhone, {
        otp,
        expires: Date.now() + 300000 // 5 minutes
    });

    return otp;
}

function verifyOTP(parentPhone, enteredOTP) {

    const record = otpStore.get(parentPhone);

    if (!record) return false;

    if (record.expires < Date.now()) {
        otpStore.delete(parentPhone);
        return false;
    }

    if (record.otp === enteredOTP) {
        otpStore.delete(parentPhone);
        return true;
    }

    return false;
}

module.exports = { generateOTP, verifyOTP };