package com.hfx.smsgold.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public final class PhoneNumberUtils {

  private PhoneNumberUtils() {
  }

  public static String preparePhoneNumber(final String phoneNumber) {
    // remove extra characters
    String preparedPhoneNumber = removeChars(phoneNumber, '+', ' ', '(', ')');

    // replace '8' as leading character with '7'
    if (preparedPhoneNumber.charAt(0) == '8') {
      return "7" + preparedPhoneNumber.substring(1);
    }

    return preparedPhoneNumber;
  }

  private static String removeChars(String source, char... chars) {
    StringBuilder sb = new StringBuilder();
    CharacterIterator it = new StringCharacterIterator(source);
    for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
      switch (c) {
        case '+':
        case ' ':
        case '(':
        case ')':
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }


}
