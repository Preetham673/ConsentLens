function generateRecommendation(risk, aiPolicyRisk, dataCategories){

    const recommendations = [];

    if(risk?.riskScore >= 75){
        recommendations.push("High privacy risk detected. Consider denying consent.");
    }

    if(risk?.riskScore >= 50 && risk?.riskScore < 75){
        recommendations.push("Moderate risk app. Review permissions carefully.");
    }

    if(dataCategories && dataCategories.includes("location")){
        recommendations.push("Location data access requested.");
    }

    if(dataCategories && dataCategories.includes("contacts")){
        recommendations.push("Contacts data access requested.");
    }

    if(aiPolicyRisk && aiPolicyRisk.policyRisk === "HIGH"){
        recommendations.push("Privacy policy indicates high data usage.");
    }

    if(recommendations.length === 0){
        recommendations.push("No major privacy risks detected.");
    }

    return recommendations;
}

module.exports = {
    generateRecommendation
};