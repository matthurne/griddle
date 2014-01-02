package com.commercehub.griddle

@SuppressWarnings("PropertyName")
final class Transformers {

    static final Closure<String> noop = { String str -> str }
    static final Closure<String> trim = { String str -> str.trim() }
    static final Closure<String> lowercase = { String str -> str.toLowerCase() }
    static final Closure<String> trimLowercase = { String str -> str.trim().toLowerCase() }

}
