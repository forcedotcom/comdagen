/*
 * Copyright (c) 2018, salesforce.com, inc. All rights reserved. SPDX-License-Identifier: BSD-3-Clause For full license
 * text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen;

public enum SupportedZone {
    Generic("generic", "US", "English"), German("de", "DE", "German"), Chinese("zh", "", "Chinese"),
    Russian("ru", "", "Russian");

    private String countryCode, languageCode, languageName;

    SupportedZone(String countryCode, String languageCode, String languageName) {
        this.countryCode = countryCode;
        this.languageCode = languageCode;
        this.languageName = languageName;
    }


    public String getCountryCode() {
        return countryCode;
    }

    public String getLocale() {
        if (this == Generic)
            return "en-US";
        if (languageCode.isEmpty())
            return countryCode;
        return countryCode + "-" + languageCode;
    }

    public String getLanguage() {
        return languageCode;
    }

    public String getLanguageName() {
        return languageName;
    }

    @Override
    public String toString() {
        if (this == Generic) {
            return "x-default";
        }
        return getLocale();
    }
}
