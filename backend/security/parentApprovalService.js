const approvalStore = new Map();

function generateApprovalToken(phone){

    const token = Math.random().toString(36).substring(2);

    approvalStore.set(token,{
        phone,
        expires: Date.now() + 600000
    });

    return token;
}

function verifyApprovalToken(token){

    const record = approvalStore.get(token);

    if(!record) return false;

    if(record.expires < Date.now()){
        approvalStore.delete(token);
        return false;
    }

    approvalStore.delete(token);

    return true;
}

module.exports = { generateApprovalToken, verifyApprovalToken };