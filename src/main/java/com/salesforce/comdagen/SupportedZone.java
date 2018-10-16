/*
 * Copyright (c) 2018, salesforce.com, inc. All rights reserved. SPDX-License-Identifier: BSD-3-Clause For full license
 * text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen;

public enum SupportedZone {
    Generic("generic"), German("de"), Chinese("zh"), Russian("ru");

    private String countryCode;

    SupportedZone(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getLocale() {
        if (this == Generic) {
            return "en_US";
        }
        return countryCode;
    }

    @Override
    public String toString() {
        if (this == Generic) {
            return "x-default";
        }
        return getLocale();
    }
}
