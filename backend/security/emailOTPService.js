const nodemailer = require("nodemailer");

// In-memory OTP storage
const otpStore = new Map();

const transporter = nodemailer.createTransport({
  host: "smtp.gmail.com",
  port: 587,
  secure: false,
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS
  },
  tls: {
    rejectUnauthorized: false
  }
});

// Generate OTP
function generateOTP(email) {

  const otp = Math.floor(100000 + Math.random() * 900000).toString();

  otpStore.set(email, {
    otp,
    expires: Date.now() + 300000
  });

  console.log("Generated OTP:", otp);

  return otp;
}

// Send email
async function sendOTP(email) {

  const otp = generateOTP(email);

  try {

    const info = await transporter.sendMail({
      from: process.env.EMAIL_USER,
      to: email,
      subject: "ConsentLens Parent Verification OTP",
      text: `Your ConsentLens verification code is: ${otp}`
    });

    console.log("Email sent:", info.response);

  } catch (err) {

    console.error("EMAIL ERROR:", err);
    throw err;
  }
}

// Verify OTP
function verifyOTP(email, enteredOTP) {

  const record = otpStore.get(email);

  if (!record) return false;

  if (record.expires < Date.now()) {
    otpStore.delete(email);
    return false;
  }

  if (record.otp === enteredOTP) {
    otpStore.delete(email);
    return true;
  }

  return false;
}

module.exports = { sendOTP, verifyOTP };