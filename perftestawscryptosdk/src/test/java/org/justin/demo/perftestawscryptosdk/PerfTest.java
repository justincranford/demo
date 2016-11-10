package org.justin.demo.perftestawscryptosdk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoAlgorithm;
import com.amazonaws.encryptionsdk.jce.JceMasterKey;

/**
 * JUnit 4 test class to run performance benchmars on crypto options in AWS Encryption SDK.
 * 
 * Sample sizes:			100/1000/10000
 * Ciphertext File output:	Optional
 * Supported AES crypto:	9 (= 3 "AES GCM" master key size options * 3 "Key Derivation Function" options)
 * @author justin.cranford
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)	// Available JUnit 4.11+
@SuppressWarnings("static-method")
public final class PerfTest {
	private static boolean SKIP_FILE_TESTS = false;	// true: only do crypto tests without file write, false: do crypto tests with and without file write

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final String CLEAR_TEXT = "PLAIN TEXT, PLAIN TEXT, PLAIN TEXT, PLAIN TEXT PLAIN TEXT, PLAIN TEXT, PLAIN TEXT, PLAIN TEXT PLAIN TEXT, PLAIN TEXT, PLAIN TEXT, PLAIN TEXT PLAIN TEXT, PLAIN TEXT, PLAIN TEXT, PLAIN TEXT";
	private static final int[] WARMUP_ITERATIONS  = {100, 1000};
	private static final int[] TEST_ITERATIONS    = {100, 1000, 10000};
	private static final int   MAX_AES_KEY_LENGTH;	// ASSUMPTION: 128 for default JCE policy, 192/256 for unlimited JCE policy
	private static final CryptoAlgorithm[] CRYPTO_ALGORITHMS;
	static {
		int maxAesKeylength = 0;
		try {
			maxAesKeylength = Cipher.getMaxAllowedKeyLength("AES");	// check max Cipher key length determined by JCE policy
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);	// abnormal end
		}
		MAX_AES_KEY_LENGTH = maxAesKeylength;
		if (PerfTest.MAX_AES_KEY_LENGTH == Integer.MAX_VALUE) {
			System.out.println("NOTE: JCE policy max AES key length: unlimited");
		} else {
			System.out.println("NOTE: JCE policy max AES key length: " + PerfTest.MAX_AES_KEY_LENGTH);
		}
		if (PerfTest.MAX_AES_KEY_LENGTH >= 256) {
			CRYPTO_ALGORITHMS = new CryptoAlgorithm[]{	// fastest to slowest 128/192/256-bit Ciphers
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_NO_KDF,
				CryptoAlgorithm.ALG_AES_192_GCM_IV12_TAG16_NO_KDF,
				CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_NO_KDF,
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256,
				CryptoAlgorithm.ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256,
				CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256,
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256,
				CryptoAlgorithm.ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384,
				CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384,
			};
		} else if (PerfTest.MAX_AES_KEY_LENGTH >= 192) {
			CRYPTO_ALGORITHMS = new CryptoAlgorithm[]{	// fastest to slowest 128/192-bit Ciphers
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_NO_KDF,
				CryptoAlgorithm.ALG_AES_192_GCM_IV12_TAG16_NO_KDF,
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256,
				CryptoAlgorithm.ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256,
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256,
				CryptoAlgorithm.ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384,
			};
		} else if (PerfTest.MAX_AES_KEY_LENGTH >= 128) {
			CRYPTO_ALGORITHMS = new CryptoAlgorithm[]{	// fastest to slowest 128-bit Ciphers
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_NO_KDF,
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256,
				CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256,
			};
		} else {
			CRYPTO_ALGORITHMS = null;
			System.err.println("MAX AES key length does not allow minimum key length 128 needed by AWS Entryption SDK");
			System.exit(1);	// abnormal end
		}
	}

	@BeforeClass
	public static void beforeClass() throws IOException {
		System.out.println("***********");
		System.out.println("beforeClass");
		System.out.println("***********");
		for (final int iterations : PerfTest.WARMUP_ITERATIONS) {
			for (final CryptoAlgorithm cryptoAlgorithm : CRYPTO_ALGORITHMS) {
				System.out.println("Warming up => Count: " + iterations + " (" + cryptoAlgorithm + ")");
				doTest(false, cryptoAlgorithm, iterations, false);
			}
		}
		System.out.println(" ");
	}

	@Test
	public void Test0001_testWithFile() throws IOException {
		System.out.println("*********************");
		System.out.println("Test0001_testWithFile");
		System.out.println("*********************");
		for (final int iterations : PerfTest.TEST_ITERATIONS) {
			for (final CryptoAlgorithm cryptoAlgorithm : CRYPTO_ALGORITHMS) {
				doTest(true, cryptoAlgorithm, iterations, true);
			}
			System.out.println(" ");
		}
		System.out.println(" ");
	}

	@Test
	public void Test0002_testWithoutFile() throws IOException {
		System.out.println("************************");
		System.out.println("Test0002_testWithoutFile");
		System.out.println("************************");
		for (final int iterations : PerfTest.TEST_ITERATIONS) {
			for (final CryptoAlgorithm cryptoAlgorithm : CRYPTO_ALGORITHMS) {
				doTest(true, cryptoAlgorithm, iterations, false);
			}
			System.out.println(" ");
		}
		System.out.println(" ");
	}

	@SuppressWarnings("null")
	public static void doTest(final boolean doPrint, final CryptoAlgorithm cryptoAlgorithm, final int numIterations, final boolean doFile) throws IOException {
		if (doFile && PerfTest.SKIP_FILE_TESTS) {
			return;
		}
		final AwsCrypto awsCrypto = new AwsCrypto();
		awsCrypto.setEncryptionAlgorithm(cryptoAlgorithm);	// 1 of 9 choices
		final SecretKey masterAes128Key = generateRandomMasterEncryptionKey();
		final JceMasterKey masterKeyProvider = JceMasterKey.getInstance(masterAes128Key, "Example", "RandomKey", "AES/GCM/NoPadding"); 
		final Map<String, String> context = Collections.singletonMap("Example", "String");
		final long startNanos;	// use nanosecond timing for calculating sub-millisecond averages
		try (PrintWriter pw = (doFile ? new PrintWriter(new BufferedWriter(new FileWriter("E:/EncryptedOutFile.txt", true))) : null)) {
			startNanos = System.nanoTime();
			for (int currentMainIteration = 1; currentMainIteration < numIterations; currentMainIteration++) {
				if (doFile) {
					pw.println(awsCrypto.encryptString(masterKeyProvider, CLEAR_TEXT, context).getResult());
				} else {
					awsCrypto.encryptString(masterKeyProvider, CLEAR_TEXT, context).getResult();
				}
			}
		}
		if (!doPrint) {
			return;	// skip print
		}
		final float totalSecs        = (System.nanoTime()-startNanos)/1000000000F;	// Expect up to 7  characters, including decimal point and 3 fraction digits 
		final float averageMillis    = 1000F*totalSecs/numIterations;				// Expect up to 7  characters, including decimal point and 3 fraction digits
		final float throughputPerSec = 1000F/averageMillis;							// Expect up to 11 characters, including decimal point and 3 fraction digits
		System.out.format("Count: %5d, Time: %7.3f sec, Avg: %7.3f msec, Thru: %11.3f/sec (%s)\n", Integer.valueOf(numIterations), Float.valueOf(totalSecs), Float.valueOf(averageMillis), Float.valueOf(throughputPerSec), cryptoAlgorithm);
	}

    private static SecretKey generateRandomMasterEncryptionKey() {
    	// Question: 16 byte or 32 byte does not seem to affect tests, so what effect is this supposed to have?
        final byte[] rawKey = new byte[16]; // 16 bytes * 8 bits/byte = 128 bits
        PerfTest.SECURE_RANDOM.nextBytes(rawKey);
        return new SecretKeySpec(rawKey, "AES");
    }

////////////////////////////////////////////////////////////////////////////////////////////////
// SAMPLE OUTPUT (Intel Core2 Duo E8400 3.0GHz, 6MB Cache, 1333 MHz FSB - overclocked to 3.2GHz)
////////////////////////////////////////////////////////////////////////////////////////////////
//
//	NOTE: JCE policy max AES key length: unlimited
//	***********
//	beforeClass
//	***********
//	Warming up => Count: 100 (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 100 (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 100 (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 100 (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 100 (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 100 (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 100 (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Warming up => Count: 100 (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Warming up => Count: 100 (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Warming up => Count: 1000 (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 1000 (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 1000 (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 1000 (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 1000 (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 1000 (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 1000 (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Warming up => Count: 1000 (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Warming up => Count: 1000 (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	*********************
//	Test0001_testWithFile
//	*********************
//	Count:   100, Time:   0.075 sec, Avg:   0.750 msec, Thru:    1332.940/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.012 sec, Avg:   0.124 msec, Thru:    8076.544/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.013 sec, Avg:   0.132 msec, Thru:    7566.947/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.015 sec, Avg:   0.146 msec, Thru:    6830.744/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.020 sec, Avg:   0.195 msec, Thru:    5125.343/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.017 sec, Avg:   0.165 msec, Thru:    6060.564/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.283 sec, Avg:   2.827 msec, Thru:     353.788/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count:   100, Time:   0.320 sec, Avg:   3.200 msec, Thru:     312.464/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count:   100, Time:   0.254 sec, Avg:   2.540 msec, Thru:     393.772/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	Count:  1000, Time:   0.087 sec, Avg:   0.087 msec, Thru:   11528.062/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.095 sec, Avg:   0.095 msec, Thru:   10498.951/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.089 sec, Avg:   0.089 msec, Thru:   11228.742/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.098 sec, Avg:   0.098 msec, Thru:   10183.996/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.099 sec, Avg:   0.099 msec, Thru:   10092.199/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.100 sec, Avg:   0.100 msec, Thru:    9950.940/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.849 sec, Avg:   0.849 msec, Thru:    1178.011/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count:  1000, Time:   2.343 sec, Avg:   2.343 msec, Thru:     426.866/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count:  1000, Time:   2.261 sec, Avg:   2.261 msec, Thru:     442.342/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	Count: 10000, Time:   0.822 sec, Avg:   0.082 msec, Thru:   12171.485/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.850 sec, Avg:   0.085 msec, Thru:   11759.183/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.868 sec, Avg:   0.087 msec, Thru:   11523.587/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.912 sec, Avg:   0.091 msec, Thru:   10968.652/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   0.938 sec, Avg:   0.094 msec, Thru:   10659.302/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   0.946 sec, Avg:   0.095 msec, Thru:   10576.272/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   8.109 sec, Avg:   0.811 msec, Thru:    1233.243/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count: 10000, Time:  22.406 sec, Avg:   2.241 msec, Thru:     446.301/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count: 10000, Time:  22.243 sec, Avg:   2.224 msec, Thru:     449.584/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//
//	************************
//	Test0002_testWithoutFile
//	************************
//	Count:   100, Time:   0.008 sec, Avg:   0.083 msec, Thru:   12090.426/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.008 sec, Avg:   0.079 msec, Thru:   12648.392/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.008 sec, Avg:   0.080 msec, Thru:   12529.376/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.009 sec, Avg:   0.085 msec, Thru:   11702.779/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.009 sec, Avg:   0.089 msec, Thru:   11289.704/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.009 sec, Avg:   0.090 msec, Thru:   11148.929/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.083 sec, Avg:   0.833 msec, Thru:    1200.138/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count:   100, Time:   0.226 sec, Avg:   2.256 msec, Thru:     443.183/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count:   100, Time:   0.231 sec, Avg:   2.309 msec, Thru:     433.163/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	Count:  1000, Time:   0.080 sec, Avg:   0.080 msec, Thru:   12507.756/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.085 sec, Avg:   0.085 msec, Thru:   11795.279/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.086 sec, Avg:   0.086 msec, Thru:   11621.071/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.090 sec, Avg:   0.090 msec, Thru:   11140.842/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.098 sec, Avg:   0.098 msec, Thru:   10189.782/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.094 sec, Avg:   0.094 msec, Thru:   10642.979/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.859 sec, Avg:   0.859 msec, Thru:    1164.042/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count:  1000, Time:   2.208 sec, Avg:   2.208 msec, Thru:     452.815/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count:  1000, Time:   2.218 sec, Avg:   2.218 msec, Thru:     450.816/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	Count: 10000, Time:   0.790 sec, Avg:   0.079 msec, Thru:   12655.877/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.818 sec, Avg:   0.082 msec, Thru:   12230.331/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.826 sec, Avg:   0.083 msec, Thru:   12106.514/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.881 sec, Avg:   0.088 msec, Thru:   11347.021/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   0.915 sec, Avg:   0.092 msec, Thru:   10927.505/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   0.919 sec, Avg:   0.092 msec, Thru:   10880.086/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   7.946 sec, Avg:   0.795 msec, Thru:    1258.484/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count: 10000, Time:  22.143 sec, Avg:   2.214 msec, Thru:     451.610/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count: 10000, Time:  22.249 sec, Avg:   2.225 msec, Thru:     449.464/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)

///////////////////////////////////////////////////////////
// SAMPLE OUTPUT (Intel Core i7-4770 3.4/3.9GHz, 8MB Cache)
///////////////////////////////////////////////////////////
//
//	NOTE: JCE policy max AES key length: unlimited
//	***********
//	beforeClass
//	***********
//	Warming up => Count: 100 (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 100 (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 100 (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 100 (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 100 (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 100 (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 100 (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Warming up => Count: 100 (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Warming up => Count: 100 (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Warming up => Count: 1000 (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 1000 (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 1000 (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Warming up => Count: 1000 (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 1000 (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 1000 (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Warming up => Count: 1000 (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Warming up => Count: 1000 (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Warming up => Count: 1000 (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	*********************
//	Test0001_testWithFile
//	*********************
//	Count:   100, Time:   0.005 sec, Avg:   0.048 msec, Thru:   20752.090/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.006 sec, Avg:   0.057 msec, Thru:   17482.881/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.006 sec, Avg:   0.056 msec, Thru:   17957.693/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.005 sec, Avg:   0.051 msec, Thru:   19548.311/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.004 sec, Avg:   0.045 msec, Thru:   22263.969/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.005 sec, Avg:   0.046 msec, Thru:   21559.754/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.049 sec, Avg:   0.490 msec, Thru:    2039.047/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count:   100, Time:   0.127 sec, Avg:   1.271 msec, Thru:     786.759/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count:   100, Time:   0.128 sec, Avg:   1.279 msec, Thru:     782.032/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	Count:  1000, Time:   0.034 sec, Avg:   0.034 msec, Thru:   29710.186/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.033 sec, Avg:   0.033 msec, Thru:   30560.684/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.033 sec, Avg:   0.033 msec, Thru:   30215.590/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.036 sec, Avg:   0.036 msec, Thru:   27709.609/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.041 sec, Avg:   0.041 msec, Thru:   24657.828/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.038 sec, Avg:   0.038 msec, Thru:   26439.063/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.453 sec, Avg:   0.453 msec, Thru:    2207.656/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count:  1000, Time:   1.256 sec, Avg:   1.256 msec, Thru:     796.446/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count:  1000, Time:   1.254 sec, Avg:   1.254 msec, Thru:     797.604/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	Count: 10000, Time:   0.311 sec, Avg:   0.031 msec, Thru:   32102.893/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.328 sec, Avg:   0.033 msec, Thru:   30488.512/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.339 sec, Avg:   0.034 msec, Thru:   29505.289/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.369 sec, Avg:   0.037 msec, Thru:   27119.018/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   0.374 sec, Avg:   0.037 msec, Thru:   26728.850/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   0.379 sec, Avg:   0.038 msec, Thru:   26397.537/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   4.455 sec, Avg:   0.445 msec, Thru:    2244.893/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count: 10000, Time:  12.544 sec, Avg:   1.254 msec, Thru:     797.173/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count: 10000, Time:  12.603 sec, Avg:   1.260 msec, Thru:     793.463/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//
//	************************
//	Test0002_testWithoutFile
//	************************
//	Count:   100, Time:   0.003 sec, Avg:   0.029 msec, Thru:   34712.941/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.003 sec, Avg:   0.029 msec, Thru:   34560.840/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.003 sec, Avg:   0.031 msec, Thru:   32711.133/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count:   100, Time:   0.004 sec, Avg:   0.040 msec, Thru:   24750.869/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.004 sec, Avg:   0.036 msec, Thru:   27465.848/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.004 sec, Avg:   0.037 msec, Thru:   27287.117/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:   100, Time:   0.045 sec, Avg:   0.454 msec, Thru:    2203.223/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count:   100, Time:   0.125 sec, Avg:   1.247 msec, Thru:     801.764/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count:   100, Time:   0.124 sec, Avg:   1.237 msec, Thru:     808.176/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	Count:  1000, Time:   0.030 sec, Avg:   0.030 msec, Thru:   33339.266/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.031 sec, Avg:   0.031 msec, Thru:   32573.143/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.030 sec, Avg:   0.030 msec, Thru:   33252.922/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count:  1000, Time:   0.036 sec, Avg:   0.036 msec, Thru:   27432.867/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.035 sec, Avg:   0.035 msec, Thru:   28684.979/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.039 sec, Avg:   0.039 msec, Thru:   25675.920/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count:  1000, Time:   0.449 sec, Avg:   0.449 msec, Thru:    2225.240/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count:  1000, Time:   1.279 sec, Avg:   1.279 msec, Thru:     782.059/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count:  1000, Time:   1.336 sec, Avg:   1.336 msec, Thru:     748.648/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//
//	Count: 10000, Time:   0.364 sec, Avg:   0.036 msec, Thru:   27438.572/sec (ALG_AES_128_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.331 sec, Avg:   0.033 msec, Thru:   30239.199/sec (ALG_AES_192_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.331 sec, Avg:   0.033 msec, Thru:   30170.322/sec (ALG_AES_256_GCM_IV12_TAG16_NO_KDF)
//	Count: 10000, Time:   0.371 sec, Avg:   0.037 msec, Thru:   26929.975/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   0.374 sec, Avg:   0.037 msec, Thru:   26734.609/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   0.374 sec, Avg:   0.037 msec, Thru:   26754.518/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA256)
//	Count: 10000, Time:   5.149 sec, Avg:   0.515 msec, Thru:    1942.076/sec (ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256)
//	Count: 10000, Time:  13.468 sec, Avg:   1.347 msec, Thru:     742.494/sec (ALG_AES_192_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
//	Count: 10000, Time:  12.426 sec, Avg:   1.243 msec, Thru:     804.792/sec (ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
}