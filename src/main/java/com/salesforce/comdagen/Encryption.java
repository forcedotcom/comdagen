/*
 * Copyright (c) 2018, salesforce.com, inc. All rights reserved. SPDX-License-Identifier: BSD-3-Clause For full license
 * text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen;

import static com.google.common.math.DoubleMath.log2;

import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.util.encoders.Base64;

import kotlin.text.Charsets;

public class Encryption
{
    /**
     * Encrypt string with SCrypt algorithm It uses the same algorithm as in Salesforce Commerce Cloud
     * SecurityMgrImpl.encryptPasswordUsingSCrypt
     *
     * @param passwd string that should get encrypted
     * @return encrypted hash
     */
    public static String scrypt( String passwd )
    {
        try
        {
            int N = 2048;
            int r = 4;
            int p = 1;

            // generate random salt
            SecureRandom secureRandom = new SecureRandom();
            byte[] salt = new byte[16];
            secureRandom.nextBytes( salt );

            // encrypt password with salt
            byte[] derived = SCrypt.generate( passwd.getBytes( Charsets.UTF_8 ), salt, N, r, p, 32 );

            String params = Long.toString( (long) log2( N ) << 16L | r << 8 | p, 16 );

            // build hash string
            StringBuilder sb = new StringBuilder( ( salt.length + derived.length ) * 2 );
            sb.append( "$s0$" ).append( params ).append( '$' );
            sb.append( new String( Base64.encode( salt ), Charsets.UTF_8 ) ).append( '$' );
            sb.append( new String( Base64.encode( derived ), Charsets.UTF_8 ) );

            return sb.toString();
        }
        catch ( SecurityException e )
        {
            throw new IllegalStateException( "JVM doesn't support HMAC_SHA256" );
        }
    }
}
