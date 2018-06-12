/*
 * Copyright (c) 2018, salesforce.com, inc. All rights reserved. SPDX-License-Identifier: BSD-3-Clause For full license
 * text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce;

/**
 * Reports a invalid specification document for a particular generator. The exception message should state which part of
 * the specification was invalid and why.
 */
public class InvalidSpecificationException
    extends RuntimeException
{
    public InvalidSpecificationException( String message )
    {
        super( message );
    }
}
