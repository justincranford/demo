package org.justin.demo.perftestawscryptosdk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;

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
 * @author justin.cranford
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("static-method")
public final class PerfTest {
	private static boolean SKIP_FILE_TESTS = false;	// true: only do crypto tests without file write, false: do crypto tests with and without file write

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final String CLEAR_TEXT = "PLAIN TEXT, PLAIN TEXT, PLAIN TEXT, PLAIN TEXT PLAIN TEXT, PLAIN TEXT, PLAIN TEXT, PLAIN TEXT PLAIN TEXT, PLAIN TEXT, PLAIN TEXT, PLAIN TEXT PLAIN TEXT, PLAIN TEXT, PLAIN TEXT, PLAIN TEXT";
	private static final int[] WARMUP_ITERATIONS = {100, 1000};
	private static final int[] TEST_ITERATIONS   = {100, 1000, 10000};
	private static final CryptoAlgorithm[] CRYPTO_ALGORITHMS = {	// fastest to slowest
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
		final long  totalNanos       = System.nanoTime() - startNanos;
		final float totalMillis      = totalNanos/1000000F;				// Expect up to 10 characters, including decimal point and 3 fraction digits 
		final float averageMillis    = totalMillis/numIterations;		// Expect up to 7  characters, including decimal point and 3 fraction digits
		final float throughputPerSec = 1000F/averageMillis;				// Expect up to 11 characters, including decimal point and 3 fraction digits
		System.out.format("Count: %5d, Time: %10.3f msec, Avg: %7.3f msec, Thru: %11.3f/sec (%s)\n", numIterations, totalMillis, averageMillis, throughputPerSec, cryptoAlgorithm);
	}

    private static SecretKey generateRandomMasterEncryptionKey() {
    	// Question: 16 byte or 32 byte does not seem to affect tests, so what effect is this supposed to have?
        final byte[] rawKey = new byte[16]; // 16 bytes * 8 bits/byte = 128 bits
        PerfTest.SECURE_RANDOM.nextBytes(rawKey);
        return new SecretKeySpec(rawKey, "AES");
    }
}