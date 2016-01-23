package com.li72.test.other;

import com.li72.test.ValidateCode;

public class ValidTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		   String verifyCode = ValidateCode.generateTextCode(ValidateCode.TYPE_NUM_ONLY, 4, null);
		   System.out.println(verifyCode);

	}

}
