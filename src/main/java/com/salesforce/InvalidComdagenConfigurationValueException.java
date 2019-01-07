/*
 *  Copyright (c) 2019, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce;

/**
 * Reports invalid comdagen configuration values such as min/max value pairs when the min is greater than the max.
 */
public class InvalidComdagenConfigurationValueException extends RuntimeException {
    public InvalidComdagenConfigurationValueException(String s) {
        super(s);
    }
}
