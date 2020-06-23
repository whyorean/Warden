/*
 * Warden
 * Copyright (C) 2020, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.aurora.warden.utils.app;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

import com.aurora.warden.utils.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public final class CertificateUtil {

    private static CertificateFactory certificateFactory;

    public static List<X509Certificate> getX509Certificates(String packageName, PackageManager packageManager) {
        final List<X509Certificate> x509Certificates = new ArrayList<>();
        try {
            @SuppressLint("PackageManagerGetSignatures") final PackageInfo pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (certificateFactory == null)
                certificateFactory = CertificateFactory.getInstance("X509");

            if (pkgInfo != null) {
                for (Signature signature : pkgInfo.signatures) {
                    byte[] cert = signature.toByteArray();
                    InputStream inStream = new ByteArrayInputStream(cert);
                    x509Certificates.add((X509Certificate) certificateFactory.generateCertificate(inStream));
                }
            }
        } catch (NameNotFoundException | CertificateException e) {
            Log.e(e.getMessage());
        }
        return x509Certificates;
    }

    public static String getCertificateFingerprint(X509Certificate x509Certificate, String hashAlgorithm) {
        String hash;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            final byte[] rawCert = x509Certificate.getEncoded();
            hash = toHexString(messageDigest.digest(rawCert));
            messageDigest.reset();
        } catch (CertificateEncodingException | NoSuchAlgorithmException e) {
            hash = "Unavailable";
        }
        return hash;
    }

    public static String toHexString(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}

