function PrivacyProfile(profile) {
  return {
    appName: profile.appName,
    dataCategories: profile.dataCategories,
    thirdPartySharing: profile.thirdPartySharing,
    policyClarity: profile.policyClarity,
    regulatoryFlags: profile.regulatoryFlags
  };
}

module.exports = PrivacyProfile;
