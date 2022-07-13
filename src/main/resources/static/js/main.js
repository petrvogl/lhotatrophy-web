/**
 * Reference na knihovnu LT.
 */
var LT = LT || {};

/**
 * Zalozeni namespace pro funkce s obecnym trivialnim chovanim.
 */
LT.function = LT.function || {};

/**
 * Tato funkce je tzv. prázdná operace (no operation) a nevykonává žádný kód.
 * Použije se tam, kde API vyžaduje callback funkci, ale my nechceme, aby se při
 * volani callbacku provedl nějaký kód.
 * <p>
 * Příklad použití:</p>
 * <pre>
 * let onCompleteFn = function (x, y) { ... }
 * let requestParams = {
 *     url: LT.context.baseUrl + "/record-video-playback",
 *     timeout: timeout,
 *     data: {videoId: videoId},
 *     onComplete: onCompleteFn,
 *     onError: LT.function.noop,
 *     onTimeout: LT.function.noop
 * };
 * </pre>
 */
LT.function.noop = function () {};

/**
 * Tato funkce vrací svůj nezměněný vstup. Respektive vrací svůj první argument.
 * Pokud argument není zadán, vrací <code>undefined</code>. Použije se tam, kde
 * API vyžaduje callback funkci, která má transformovat předaný argument, ale my
 * nechceme, aby se při volani callbacku nějaká transformace provedla.
 *
 * @param arg vstup funkce
 * @returns Vrací nezměněný vstupní argument <code>arg</code>
 */
LT.function.identity = function (arg) {
	return arg;
};

/*
 *	EXCEPTIONS
 */

/**
 * Trida vyjimky, ktera oznamuje, ze funkci byly predany nevhodne nebo
 * nepovolene argumenty.
 *
 * @extends Error
 *
 * @constructor
 * @param {String|Object} message Detailni zprava o chybe
 */
LT.IllegalArgumentException = function (message) {
	this.message = message;
};
// class IllegalArgumentException extends Error
LT.IllegalArgumentException.prototype = new Error();

/**
 * Tato vyjimka by se mela vyhazovat pro oznameni nevhodneho nebo nepovoleneho
 * pouziti null (pripadne undefined).
 *
 * Pro oznamovani konkretniho stavu stavu, kdy referovana promenna nebyla
 * deklarovana, pouzij vestavenou tridu ReferenceError.
 *
 * @extends Error
 *
 * @constructor
 * @param {String|Object} message Detailni zprava o chybe
 */
LT.NullPointerException = function (message) {
	this.message = message;
};
// class IllegalArgumentException extends Error
LT.NullPointerException.prototype = new Error();

/**
 * Vyjimka je vyhazovana pri hledani nebo pristupu k elementu v DOM a oznamuje,
 * ze pozadovany HTML element v dokumentu neexistuje.
 *
 * @extends Error
 *
 * @constructor
 * @param {String|Object} message
 */
LT.ElementNotFoundException = function (message) {
	this.message = message;
};
// class ElementNotFoundException extends Error
LT.ElementNotFoundException.prototype = new Error();

/**
 * Tato vyjimka oznamuje, ze funkce byla vyvolana v nevhodnem nebo nepovolenem
 * okamziku. Tedy ze prostredi, aplikace nebo objekt nejsou ve vhodnem stavu
 * pro volani pozadovane operace.
 *
 * @extends Error
 *
 * @constructor
 * @param {String|Object} message
 */
LT.IllegalStateException = function (message) {
	this.message = message;
};
// class IllegalStateException extends Error
LT.IllegalStateException.prototype = new Error();

/*
 *	VALUE TESTING AND PRECONDITIONS
 */

/**
 * Vraci true v pripade, ze parametr neni definovan, jinak vraci false.
 *
 * @param {Object} value Testovana hodnota
 */
LT.isUndefined = function (value) {
	return typeof value === "undefined";
};
LT.isNotUndefined = function (value) {
	return !LT.isUndefined(value);
};

/**
 * Vraci true v pripade, ze parametr je null, jinak vraci false.
 *
 * @param {Object} value Testovana hodnota
 */
LT.isNull = function (value) {
	return value === null;
};
LT.isNotNull = function (value) {
	return !LT.isNull(value);
};

/**
 * Vraci <code>true</code> v pripade, kdy parametr je <code>undefined</code>,
 * <code>null</code> nebo <code>""</code>, jinak vraci <code>false</code>.
 *
 * @param {String|Number|Boolean} value Testovana hodnota
 */
LT.isEmpty = function (value) {
	return typeof value === "undefined" || value === null || value === "";
};
LT.isNotEmpty = function (value) {
	return !LT.isEmpty(value);
};

/**
 * Vraci true v pripade, ze lze parametr vyhodnotit jako true, jinak vraci false.
 * <p>
 * Priklady chovani:</p>
 * <pre>
 * console.assert(LT.isTrue(undefined) === false);
 * console.assert(LT.isTrue(null) === false);
 * console.assert(LT.isTrue(-1) === true);
 * console.assert(LT.isTrue(0) === false);
 * console.assert(LT.isTrue(1) === true);
 * console.assert(LT.isTrue(2) === true);
 * console.assert(LT.isTrue('') === false);
 * console.assert(LT.isTrue('true') === true);
 * console.assert(LT.isTrue('false') === false);
 * console.assert(LT.isTrue({}) === true);
 * console.assert(LT.isTrue([]) === true);
 * console.assert(LT.isTrue(LT) === true);
 * console.assert(LT.isTrue(LT.isTrue) === true);
 * console.assert(LT.isTrue(undefined, 123) === 123);</pre>
 *
 * @param {Object} value Testvana hodnota
 * @param {Object} defaultValue Nepovinna defaultni hodnota pokud value je false
 */
LT.isTrue = function (value, defaultValue) {
	return LT.isUndefined(value)
			? (LT.isUndefined(defaultValue) ? false : defaultValue)
			: value !== "false" && !!value;
};

/**
 * Vraci true v pripade, ze lze parametr vyhodnotit jako false, jinak vraci false.
 * <p>
 * Priklady chovani:</p>
 * <pre>
 * console.assert(LT.isFalse(undefined) === true);
 * console.assert(LT.isFalse(null) === true);
 * console.assert(LT.isFalse(-1) === false);
 * console.assert(LT.isFalse(0) === true);
 * console.assert(LT.isFalse(1) === false);
 * console.assert(LT.isFalse(2) === false);
 * console.assert(LT.isFalse('') === true);
 * console.assert(LT.isFalse('true') === false);
 * console.assert(LT.isFalse('false') === true);
 * console.assert(LT.isFalse({}) === false);
 * console.assert(LT.isFalse([]) === false);
 * console.assert(LT.isFalse(LT) === false);
 * console.assert(LT.isFalse(LT.isTrue) === false);
 * console.assert(LT.isFalse(undefined, 123) === 123);</pre>
 *
 * @param {Object} value Testvana hodnota
 * @param {Object} defaultValue Nepovinna defaultni hodnota pokud value je true
 */
LT.isFalse = function (value, defaultValue) {
	return LT.isUndefined(value)
			? (LT.isUndefined(defaultValue) ? true : defaultValue)
			: value === "false" || (value !== "true" && !value);
};

/**
 * Vraci <code>true</code> v pripade, ze parametr je typu <code>string</code>,
 * jinak vraci <code>false</code>. Pojud je paramnetr <code>null</code> nebo
 * <code>undefined</code>, tak vati <code>false</code>.
 *
 * @param {Object} value Testovana hodnota
 */
LT.isString = function (value) {
	return (
			value !== null &&
			typeof value !== "undefined" &&
			(typeof value === "string" || value instanceof String)
			);
};

/**
 * Vraci <code>true</code> v pripade, ze parametr je typu <code>Array</code>,
 * jinak vraci <code>false</code>. Pojud je paramnetr <code>null</code> nebo
 * <code>undefined</code>, tak vati <code>false</code>.
 *
 * @param {Object} value Testovana hodnota
 */
LT.isArray = function (value) {
	return (
			value !== null &&
			typeof value !== "undefined" &&
			typeof value === "object" &&
			value instanceof Array
			);
};

/**
 * Vraci <code>true</code> v pripade, ze parametr je funkce, jinak vraci
 * <code>false</code>. Pojud je paramnetr <code>null</code> nebo
 * <code>undefined</code>, tak vati <code>false</code>.
 *
 * @param {Object} value Testovana hodnota
 */
LT.isFunction = function (value) {
	return (value !== null
			&& typeof value !== "undefined"
			&& (typeof value === "function" || typeof value === "Function"
					|| value instanceof Function)
			);
};

/**
 * Vraci <code>true</code> v pripade, ze parametr je typu <code>jQuery object</code>,
 * jinak vraci <code>false</code>. Pojud je paramnetr <code>null</code> nebo
 * <code>undefined</code>, tak vati <code>false</code>.
 *
 * @param {Object} value Testovana hodnota
 */
LT.isJQueryObject = function (value) {
	return (
			value !== null &&
			typeof value !== "undefined" &&
			typeof value === "object" &&
			LT.isTrue(value.jquery)
			);
};

/**
 * Vraci <code>true</code> v pripade, ze parametr je typu <code>DOM node</code>,
 * jinak vraci <code>false</code>. Pojud je paramnetr <code>null</code> nebo
 * <code>undefined</code>, tak vati <code>false</code>.
 *
 * @param {Object} value Testovana hodnota
 */
LT.isDomNode = function (value) {
	return (
			value !== null &&
			typeof value !== "undefined" &&
			(typeof Node instanceof Object
					? // DOM2
					value instanceof Node
					: // FF3, IE7, Chrome 1 and Opera 9
					typeof value === "object" &&
					typeof value.nodeType === "number" &&
					typeof value.nodeName === "string")
			);
};

/**
 * Vraci <code>true</code> v pripade, ze parametr je typu <code>DOM element</code>,
 * jinak vraci <code>false</code>. Pokud je paramnetr <code>null</code> nebo
 * <code>undefined</code>, tak vrati <code>false</code>.
 *
 * @param {Object} value Testovana hodnota
 */
LT.isDomElement = function (value) {
	return (
			value !== null &&
			typeof value !== "undefined" &&
			(typeof HTMLElement instanceof Object
					? // DOM2
					value instanceof HTMLElement
					: // FF3, IE7, Chrome 1 and Opera 9
					typeof value === "object" &&
					value !== null &&
					value.nodeType === 1 &&
					typeof value.nodeName === "string")
			);
};

/**
 * Otestuje prvni parametr metody a pokud neni null ani undefined, tak ho vrati.
 * Pokud test selze, tak vyhodi vyjimku LT.NullPointerException.
 * <p>
 * LT.checkNotNull(value [, message [, funcParam1...]]) </p>
 * <p>
 * Priklady:</p>
 * <pre>
 * LT.checkNotNull(value, "Nesmi byt null");
 * LT.checkNotNull(value, function () {return "Nesmi byt null";});
 * LT.checkNotNull(
 *    value,
 *    function (params) {return "Prvek ID=" + params[0] + " nesmi byt null";},
 *    itemId);</pre>
 *
 * @param {Object} value
 * @param {String|Function} message nepovinny
 * @param {Object} funcParam1 nepovinny
 *
 * @returns {Object} Vrati nezmenenou testovanou hodnotu.
 *
 * @throws {LT.NullPointerException} Pokud prvni parametr je null nebo undefined
 */
LT.checkNotNull = function () {
	if (arguments.length === 0) {
		// nejmene jeden argument musi byt zadan
		throw new LT.IllegalArgumentException(
				"LT.checkNotNull() call with missing arguments."
				);
	}
	const args = Array.prototype.slice.call(arguments);
	const value = args[0];
	if (LT.isNotNull(value) && LT.isNotUndefined(value)) {
		// podminka je splnena, vratim testovanou hodnotu
		return value;
	}
	const msgSupplier = args[1];
	// ziskam subarray od indexu 2 do konce
	const msgParams = args.slice(2);
	if (LT.isFalse(msgSupplier)) {
		throw new LT.NullPointerException("Value must not be null.");
	}
	// msgSupplier muze byt funkce nebo string
	const message =
			typeof msgSupplier === "function"
			? (msgParams.length === 0 ? msgSupplier() : msgSupplier(msgParams))
			: msgSupplier;
	throw new LT.NullPointerException(message);
};

/**
 * Otestuje jestli ma pravdivostni vyraz 'expression' hodnotu true. Pokud test
 * selze, tak vyhodi vyjimku LT.IllegalArgumentException.
 * <p>
 * LT.checkArgument(expression [, message [, funcParam1...]]) </p>
 * <p>
 * Priklady:</p>
 * <pre>
 * LT.checkArgument(value > 100, "Musi byt > 100");
 * LT.checkArgument(value > 100, function () {return "Musi byt > 100";});
 * LT.checkArgument(
 *    value > 100,
 *    function (params) {return "Prvek ID=" + params[0] + " musi byt > 100";},
 *    itemId);</pre>
 *
 * @param {Boolean} expression
 * @param {String|Function} message nepovinny
 * @param {Object} funcParam1 nepovinny
 *
 * @throws {LT.IllegalArgumentException} Pokud ma expression hodnotu false
 */
LT.checkArgument = function () {
	if (arguments.length === 0) {
		// nejmene jeden argument musi byt zadan
		throw new LT.IllegalArgumentException("LT.checkArgument() call with missing arguments.");
	}
	const args = Array.prototype.slice.call(arguments);
	const expression = args[0];
	if (LT.isTrue(expression)) {
		// podminka je splnena, ukoncim metodu
		return;
	}
	const msgSupplier = args[1];
	// ziskam subarray od indexu 2 do konce
	const msgParams = args.slice(2);
	if (LT.isFalse(msgSupplier)) {
		throw new LT.IllegalArgumentException("Argument has illegal value.");
	}
	// msgSupplier muze byt funkce nebo string
	const message =
			typeof msgSupplier === "function"
			? (msgParams.length === 0 ? msgSupplier() : msgSupplier(msgParams))
			: msgSupplier;
	throw new LT.IllegalArgumentException(message);
};

/**
 * Otestuje jestli ma pravdivostni vyraz 'expression' hodnotu true. Pokud test
 * selze, tak vyhodi vyjimku LT.IllegalStateException.
 * <p>
 * LT.checkState(expression [, message [, funcParam1...]]) </p>
 * <p>
 * Priklady:</p>
 * <pre>
 * LT.checkState(obj.isReady(), "Musi byt pripraven");
 * LT.checkState(obj.isReady(), function () {return "Musi byt pripraven";});
 * LT.checkState(
 *    obj.isReady(),
 *    function (params) {return "Prvek ID=" + params[0] + " musi byt pripraven";},
 *    itemId);</pre>
 *
 * @param {Boolean} expression
 * @param {String|Function} message nepovinny
 * @param {Object} funcParam1 nepovinny
 *
 * @throws {LT.IllegalStateException} Pokud ma expression hodnotu false
 */
LT.checkState = function () {
	if (arguments.length === 0) {
		// nejmene jeden argument musi byt zadan
		throw new LT.IllegalStateException("LT.checkState() call with missing arguments.");
	}
	const args = Array.prototype.slice.call(arguments);
	const expression = args[0];
	if (LT.isTrue(expression)) {
		// podminka je splnena, ukoncim metodu
		return;
	}
	const msgSupplier = args[1];
	// ziskam subarray od indexu 2 do konce
	const msgParams = args.slice(2);
	if (LT.isFalse(msgSupplier)) {
		throw new LT.IllegalStateException("Illegal state.");
	}
	// msgSupplier muze byt funkce nebo string
	const message =
			typeof msgSupplier === "function"
			? (msgParams.length === 0 ? msgSupplier() : msgSupplier(msgParams))
			: msgSupplier;
	throw new LT.IllegalStateException(message);
};

/*
 *	UTILITIES
 */

/**
 * StringBuilder
 *
 * @constructor
 * @param {String} initialValue
 */
LT.StringBuilder = function StringBuilder(initialValue) {
	this._buffer = new Array();
	if (LT.isNotUndefined(initialValue)) {
		this.append(initialValue);
	}
};

LT.StringBuilder.prototype.append = function (str) {
	if (LT.isNotUndefined(str)) {
		if (str === null) {
			this._buffer.push("null");
		} else if (str === true) {
			this._buffer.push("true");
		} else if (str === false) {
			this._buffer.push("false");
		} else {
			this._buffer.push(str);
		}
	}
	return this;
};

LT.StringBuilder.prototype.clear = function () {
	this._buffer = [];
	return this;
};

LT.StringBuilder.prototype.toString = function () {
	return this._buffer.join("");
};

LT.randomString = function (length) {
	LT.checkArgument(
			length > 0,
			"Delka nahodneho retezce musi byt kladne cislo."
			);
	let res = "";
	for (var i = 0; i < length; i++) {
		// ASCII/UTF-8  a-z
		let charCode = Math.floor(Math.random() * (122 - 97)) + 97;
		res += String.fromCharCode(charCode);
	}
	return res;
};

/**
 * A container object which may or may not contain a non-null value.
 * If a value is present, <code>isPresent()</code> will return <code>true</code>
 * and <code>get()</code> will return the value.
 *
 * // private
 * @constructor Constructs an empty instance.
 */
LT.Optional = function Optional() {
	this._value = null;
};

// Generally only one empty instance, should exist.
LT.Optional.EMPTY = new LT.Optional();

/**
 * Returns an empty <code>LT.Optional</code> instance. No value is present for
 * this Optional.
 *
 * @static
 * @returns {LT.Optional}
 */
LT.Optional.empty = function () {
	return LT.Optional.EMPTY;
};

/**
 * Returns an <code>LT.Optional</code> with the specified present non-null value.
 *
 * @static
 * @param {Object} value the value to be present, which must be non-null
 * @returns {LT.Optional} an <code>LT.Optional</code> with the value present
 * @throws {LT.NullPointerException} if value is null
 */
LT.Optional.of = function (value) {
	let opt = new LT.Optional(); // is empty
	opt._value = LT.checkNotNull(value); // sets the value
	return opt;
};

/**
 * Returns an <code>LT.Optional</code> describing the specified value,
 * if non-null, otherwise returns an empty <code>LT.Optional<code>.
 *
 * @static
 * @param {Object} value the possibly-null value to describe
 * @returns {LT.Optional} an <code>LT.Optional</code> with a present value if
 * the specified value is non-null, otherwise an empty <code>LT.Optional<code>
 */
LT.Optional.ofNullable = function (value) {
	return LT.isUndefined(value) || LT.isNull(value)
			? LT.Optional.empty()
			: LT.Optional.of(value);
};

/**
 * If a value is present in this <code>LT.Optional</code>, returns the value,
 * otherwise throws <code>LT.NoSuchElementException</code>.
 *
 * @return {Object} the non-null value held by this <code>LT.Optional</code>
 * @throws {LT.NoSuchElementException} if there is no value present
 */
LT.Optional.prototype.get = function () {
	if (this._value === null) {
		throw new LT.NoSuchElementException("No value present");
	}
	return this._value;
};

/**
 * Return <code>true</code> if there is a value present,
 * otherwise <code>false</code>.
 */
LT.Optional.prototype.isPresent = function () {
	return this._value !== null;
};

/**
 * If a value is present, invoke the specified consumer with the value,
 * otherwise do nothing.
 *
 * @param {Function} consumer <code>consumer(value)</code> to be executed if
 * a value is present
 * @throws {LT.NullPointerException} if value is present and
 * <code>consumer</code> is null
 */
LT.Optional.prototype.ifPresent = function (consumer) {
	if (this._value !== null) {
		LT.checkNotNull(consumer);
		consumer(this._value);
	}
};

/**
 * If a value is present, and the value matches the given predicate,
 * return an <code>LT.Optional</code> describing the value, otherwise return an
 * empty <code>LT.Optional</code>.
 *
 * @param {Function} predicate a <code>predicate(value):boolean</code> to apply
 * to the value, if present
 * @returns {LT.Optional} an <code>LT.Optional</code> describing the value of
 * this <code>LT.Optional</code> if a value is present and the value matches
 * the given predicate, otherwise an empty <code>LT.Optional</code>
 * @throws {LT.NullPointerException} if the predicate is null
 */
LT.Optional.prototype.filter = function (predicate) {
	LT.checkNotNull(predicate);
	if (!this.isPresent()) {
		return this;
	}
	return predicate(this._value) ? this : LT.Optional.empty();
};

/**
 * If a value is present, apply the provided mapping function to it, and if
 * the result is non-null, return an <code>LT.Optional</code> describing
 * the result. Otherwise return an empty <code>LT.Optional</code>.
 *
 * @param {Function} mapper a <code>mapper(value):Object</code> function to
 * apply to the value, if present
 * @returns {LT.Optional} an <code>LT.Optional</code> describing the result of
 * applying a mapping function to the value of this <code>LT.Optional<code>,
 * if a value is present, otherwise an empty <code>LT.Optional</code>
 * @throws {LT.NullPointerException} if the mapping function is null
 */
LT.Optional.prototype.map = function (mapper) {
	if (!this.isPresent()) {
		return this;
	}
	LT.checkNotNull(mapper);
	return LT.Optional.ofNullable(mapper(this._value));
};

/**
 * Return the value if present, otherwise return <code>other</code>.
 *
 * @param {Object} other the value to be returned if there is no value present,
 * may be null
 * @returns {Object} the value, if present, otherwise <code>other</code>
 */
LT.Optional.prototype.orElse = function (other) {
	return this._value !== null ? this._value : other;
};

/**
 * Return the value if present, otherwise invoke <code>other</code> and return
 * the result of that invocation.
 *
 * @param {Function} otherSupplier a <code>otherSupplier():Object</code> whose
 * result is returned if no value is present
 * @returns {Object} the value if present otherwise the result of
 * <code>otherSupplier()</code>
 * @throws {LT.NullPointerException} if value is not present and
 * <code>otherSupplier</code> is null
 */
LT.Optional.prototype.orElseGet = function (otherSupplier) {
	if (this.isPresent()) {
		return this._value;
	}
	LT.checkNotNull(otherSupplier);
	return otherSupplier();
};

/**
 * Zalozeni namespace pro funkce pro podporu supplieru.
 */
LT.Suppliers = LT.Suppliers || {};

/**
 * Returns a supplier function which caches the instance retrieved during the
 * first call and returns that value on subsequent calls.
 *
 * @param {Function} delegate Original supplier function
 * @returns {Function} Memoized supplier
 *
 * @see <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
 */
LT.Suppliers.memoize = function (delegate) {
	LT.checkNotNull(delegate, "Supplier delegate must not be null.");
	LT.checkArgument(LT.isFunction(delegate), "Supplier delegate must be function.");
	// memoized state of supplier
	let supplierState = {
		delegate: delegate,
		initialized: false,
		value: null
	};
	// memoizing supplier function
	let memoizingSupplier = function () {
		if (!supplierState.initialized) {
			let suppliedValue = supplierState.delegate();
			supplierState.value = suppliedValue;
			supplierState.initialized = true;
			return suppliedValue;
		}
		return supplierState.value;
	};

	return memoizingSupplier;
};

/*
 *	LOGGING
 */

/**
 * Logger
 *
 * @constructor
 */
LT.Logger = function Logger() {
	this.COLOR_DEBUG = "background: #aaffaa;";
	this.COLOR_INFO = "background: #aaaaff;";
	this.COLOR_WARNING = "background: #ffaa55; color: #000000;";
	this.COLOR_ERROR = "background: #ff5555; color: #000000;";
	this.COLOR_FATAL = "background: #000000; color: #ffffff;";

	// defaultne se v konzoli loguje od DEBUG
	this.loggingLevel = LT.Logger.SeverityLevelEnum.DEBUG;
};

LT.Logger.prototype.SeverityLevelEnum = {
	DEBUG: 1,
	INFO: 2,
	WARNING: 3,
	ERROR: 4,
	FATAL: 5
};
// public static class LT.Logger.SeverityLevelEnum
LT.Logger.SeverityLevelEnum = LT.Logger.prototype.SeverityLevelEnum;

LT.Logger.prototype.setLoggingLevel = function (severityLevel) {
	LT.checkNotNull(severityLevel, "Severity level must not be null");
	this.loggingLevel = severityLevel;
};

/**
 * @private
 */
LT.Logger.prototype._doLog = function (severityLevel, args) {
	if (window.console && window.console.log) {
		let loggedObject = args.length === 1 ? args[0] : args;
		switch (severityLevel) {
			case LT.Logger.SeverityLevelEnum.DEBUG:
				window.console.log("%c DEBUG: ", this.COLOR_DEBUG, loggedObject);
				break;
			case LT.Logger.SeverityLevelEnum.INFO:
				window.console.log("%c INFO: ", this.COLOR_INFO, loggedObject);
				break;
			case LT.Logger.SeverityLevelEnum.WARNING:
				window.console.warn("%c WARNING: ", this.COLOR_WARNING, loggedObject);
				break;
			case LT.Logger.SeverityLevelEnum.ERROR:
				window.console.error("%c ERROR: ", this.COLOR_ERROR, loggedObject);
				break;
			case LT.Logger.SeverityLevelEnum.FATAL:
				window.console.error("%c FATAL: ", this.COLOR_FATAL, loggedObject);
				break;
		}
	}
};

LT.Logger.prototype.debug = function () {
	let severityLevel = LT.Logger.SeverityLevelEnum.DEBUG;
	if (this.loggingLevel === severityLevel) {
		this._doLog(severityLevel, arguments);
	}
};

LT.Logger.prototype.info = function () {
	let severityLevel = LT.Logger.SeverityLevelEnum.INFO;
	if (this.loggingLevel <= severityLevel) {
		this._doLog(severityLevel, arguments);
	}
};

LT.Logger.prototype.warn = function () {
	let severityLevel = LT.Logger.SeverityLevelEnum.WARNING;
	if (this.loggingLevel <= severityLevel) {
		this._doLog(severityLevel, arguments);
	}
};

LT.Logger.prototype.error = function () {
	let severityLevel = LT.Logger.SeverityLevelEnum.ERROR;
	if (this.loggingLevel <= severityLevel) {
		this._doLog(severityLevel, arguments);
	}
};

LT.Logger.prototype.fatal = function () {
	let severityLevel = LT.Logger.SeverityLevelEnum.FATAL;
	if (this.loggingLevel <= severityLevel) {
		this._doLog(severityLevel, arguments);
	}
};

LT.Logger.prototype.isDebugEnabled = function () {
	return this.loggingLevel === LT.Logger.SeverityLevelEnum.DEBUG;
};

LT.Logger.prototype.isInfoEnabled = function () {
	return this.loggingLevel <= LT.Logger.SeverityLevelEnum.INFO;
};

/**
 * Nastaveni globalniho loggeru
 */
LT.log = LT.log || new LT.Logger();

/*
 *	CACHE
 */

/**
 * Register typu [key, value]
 *
 * @constructor
 */
LT.Register = function Register() {
	this._entries = {};
	this._size = 0;
};

/**
 * Vytvoreni registru z dvojic parametru (klic, hodnota).
 * <pre>
 * LT.Register.fromArray(array)
 * LT.Register.fromArray(key, value [, key2, value2 [, key3, value3...]])</pre>
 *
 * @static
 * @param {Array}
 * @returns {LT.Register}
 */
LT.Register.fromArray = function () {
	const args = Array.prototype.slice.call(arguments);
	const argsCount = args.length;
	if (argsCount === 0) {
		// zadne parametry -> vratim prazdny registr
		return new LT.Register();
	}
	let entries = args;
	let size = argsCount;
	// pokud vstup je pole
	if (argsCount === 1 && typeof args[0] === "object" && args[0] instanceof Array) {
		entries = args[0];
		size = args[0].length;
	}
	LT.checkArgument(size % 2 === 0, function () {
		// message supplier
		return ("Parametry musi byt dvojice (klic, hodnota) a jejich pocet musi byt sudy. Pocet parametru je " + size);
	});
	let register = new LT.Register();
	for (var i = 0; i < size; i += 2) {
		register.put(entries[i], entries[i + 1]);
	}
	return register;
};

/**
 * Vytvoreni melke kopie (shallow copy) registru. Pozdejsi vlozeni prvku
 * do puvodniho registru neovlivni jeho kopii a obracene.
 * <pre>
 * var orig = LT.Register.fromArray('key', 'value');
 * var copy = orig.cone();</pre>
 *
 * @returns {LT.Register}
 */
LT.Register.prototype.clone = function () {
	let copy = new LT.Register();
	this.forEachEntry(function (key, value) {
		copy.put(key, value);
	});
	return copy;
};

/**
 * Vlozi pod identifikatorem 'key' do registru hodnotu 'value'.
 *
 * @param {String|Number} key Unikatni klic identifikujici ulozena data
 * @param {String|Number|Boolean|Object|Function} value Ukladana data
 *
 * @throws {LT.NullPointerException} Pokud 'key' je <code>null</code>.
 * @throws {LT.IllegalArgumentException} Pokud 'value' je <code>undefined</code>.
 */
LT.Register.prototype.put = function (key, value) {
	// klic nesmi byt null ani undefined
	LT.checkNotNull(key, "Key must not be null");
	// hodnota nesmi byt undefined
	LT.checkArgument(LT.isNotUndefined(value), "Value must not be 'undefined'");

	let previous = this._entries[key];
	this._entries[key] = value;
	// pokud puvodni hodnota neexistuje, tak byla pridana nova polozka
	if (LT.isUndefined(previous)) {
		this._size += 1;
	}
	return previous;
};

/**
 * Vrati z registru data podle identifikatoru 'key'.
 *
 * @param {String|Number} key Unikatni klic identifikujici ulozena data
 *
 * @throws {LT.IllegalArgumentException} Pokud 'key' je null.
 */
LT.Register.prototype.get = function (key) {
	// klic musi byt uveden
	LT.checkNotNull(key, "Key must not be null");
	return this._entries[key];
};

/**
 * Odstrani z registru data ulozena pod identifikatorem 'key'.
 *
 * @param {String|Number} key Unikatni klic identifikujici ulozena data
 *
 * @throws {LT.IllegalArgumentException} Pokud 'key' je null.
 */
LT.Register.prototype.remove = function (key) {
	// klic musi byt uveden
	LT.checkNotNull(key, "Key must not be null");

	let previous = this._entries[key];
	// pokud puvodni hodnota existuje, tak dojde k odebrani polozky
	if (LT.isNotUndefined(previous)) {
		delete this._entries[key];
		this._size -= 1;
	}
	return previous;
};

/**
 * Vrati <code>true</code>, pokud registr obsahuje data pro klic 'key'.
 *
 * @param {String|Number} key
 * @returns {Boolean}
 *
 * @throws {LT.IllegalArgumentException} Pokud 'key' je null.
 */
LT.Register.prototype.contains = function (key) {
	// klic musi byt uveden
	LT.checkNotNull(key, "Key must not be null");
	return LT.isNotUndefined(this._entries[key]);
};

/**
 * Smaze z registru vsechna ulozena data.
 */
LT.Register.prototype.clean = function () {
	this._entries = {};
	this._size = 0;
};

/**
 * Vrati pocet polozek, ulozenych v registru.
 */
LT.Register.prototype.size = function () {
	return this._size;
};

/**
 * Vrati <code>true</code>, pokud je registr prazdny.
 */
LT.Register.prototype.isEmpty = function () {
	return this._size === 0;
};

/**
 * Vrati pole klicu.
 *
 * @returns {Array}
 */
LT.Register.prototype.getKeys = function () {
	let keys = [];
	if (!this.isEmpty()) {
		for (var key in this._entries) {
			// pouze vlozene polozky
			if (this._entries.hasOwnProperty(key)) {
				let value = this._entries[key];
				// pouze nesmazane polozky
				if (LT.isNotUndefined(value)) {
					keys.push(key);
				}
			}
		}
	}
	return keys;
};

/**
 * Pro kazdou polozku registru provede dodanou funkci.
 * <pre>
 * register.forEachEntry( function(key, value) )</pre>
 *
 * @param {function} consumer
 *
 * @throws {LT.IllegalArgumentException} Pokud 'key' je null.
 */
LT.Register.prototype.forEachEntry = function (consumer) {
	for (var key in this._entries) {
		// pouze vlozene polozky
		if (this._entries.hasOwnProperty(key)) {
			let value = this._entries[key];
			// pouze nesmazane polozky
			if (LT.isNotUndefined(value)) {
				consumer(key, value);
			}
		}
	}
};

/**
 * Zalozeni globalniho registru dat podle klice typu String
 */
LT.cache = LT.cache || new LT.Register();

/**
 * Vrati soukromou cache podle uvedeneho jmena. Pokud cache neexistuje, tak je
 * vytvorena a vracena.
 * <p>
 * Privatni cache se ukladaji do globalni cache. Pokud se smaze globalni cache
 * <code>LT.cache.clean()</code>, tak se smazou take vsechny privatni. Privatni
 * cache maji k nazvu automaticky pridavan prefix "__".</p>
 *
 * @param {String} name Nazev soukrome kese.
 * @returns {LT.Register}
 */
LT.cache.getPrivateCache = function (name) {
	let cacheKey = "__" + name;
	let privateCache = LT.cache.get(cacheKey);
	if (LT.isUndefined(privateCache) || LT.isNull(privateCache)) {
		// soukroma cache zatim neexistuje, tak ji zalozim
		privateCache = new LT.Register();
		LT.cache.put(cacheKey, privateCache);
		LT.log.debug("Zalozena privatni cache. Name=[" + name + "]");
	}
	return privateCache;
};

/**
 * Zalozeni globalniho registru priznaku typu Boolean.
 */
LT.flags = LT.flags || {_flags: new LT.Register()};

/**
 * Vrati puvodni hodnotu priznaku a nastavi novou hodnotu na <code>true</code>.
 * Defaultni hodnota priznaku je <code>false</code>.
 *
 * @param {String} flagName Nazev priznaku
 * @returns {Boolean} Puvodni hodnota priznaku
 *
 * @throws {LT.IllegalArgumentException} Pokud 'flagName' neni <code>String</code>.
 */
LT.flags.getThenSetTrue = function (flagName) {
	LT.checkArgument(LT.isString(flagName) && flagName !== "", "Nazev priznaku musi byt neprazdny retezec.");
	let flagValue = this._flags.get(flagName);
	if (LT.isFalse(flagValue)) {
		this._flags.put(flagName, true);
		// flagValue mohlo byt undefined, proto vracim explicitne FALSE
		return false;
	}
	// flag uz je nastaven na TRUE, zustava nastaven na TRUE
	return true;
};

/**
 * Vrati puvodni hodnotu priznaku a nastavi novou hodnotu na <code>false</code>.
 * Defaultni hodnota priznaku je <code>false</code>.
 *
 * @param {String} flagName Nazev priznaku
 * @returns {Boolean} Puvodni hodnota priznaku
 *
 * @throws {LT.IllegalArgumentException} Pokud 'flagName' neni <code>String</code>.
 */
LT.flags.getThenSetFalse = function (flagName) {
	LT.checkArgument(LT.isString(flagName) && flagName !== "", "Nazev priznaku musi byt neprazdny retezec.");
	let flagValue = this._flags.get(flagName);
	if (flagValue === true) {
		this._flags.remove(flagName);
		return true;
	}
	// flagValue mohlo byt undefined, proto vracim explicitne FALSE
	return false;
};

/**
 * Nastavi hodnotu priznaku na <code>true</code>.
 *
 * @param {String} flagName Nazev priznaku
 *
 * @throws {LT.IllegalArgumentException} Pokud 'flagName' neni <code>String</code>.
 */
LT.flags.setup = function (flagName) {
	LT.flags.getThenSetTrue(flagName);
};

/**
 * Nastavi hodnotu priznaku na <code>false</code>.
 *
 * @param {String} flagName Nazev priznaku
 *
 * @throws {LT.IllegalArgumentException} Pokud 'flagName' neni <code>String</code>.
 */
LT.flags.unset = function (flagName) {
	LT.flags.getThenSetFalse(flagName);
};

/**
 * Vrati soucasnou hodnotu priznaku. Defaultni hodnota priznaku je
 * <code>false</code>.
 *
 * @param {String} flagName Nazev priznaku
 * @returns {Boolean} Soucasna hodnota priznaku
 *
 * @throws {LT.IllegalArgumentException} Pokud 'flagName' neni <code>String</code>.
 */
LT.flags.check = function (flagName) {
	LT.checkArgument(LT.isString(flagName) && flagName !== "", "Nazev priznaku musi byt neprazdny retezec.");
	let flagValue = this._flags.get(flagName);
	return flagValue === true;
};

/**
 * Zalozeni namespace pro AJAX
 */
LT.ajax = LT.ajax || {};

/**
 * Provede asynchronní HTTP (Ajax) požadavek. Pokud je zadán parametr
 * <code>interactiveObjectId</code>, tak funkce zabrani tomu, aby pro toto ID
 * byl odeslan dalsi Ajax pozadavek, dokud není předchozí pozadavek dokončen.
 *
 * @param {Object} params Parametry požadavku podle https://api.jquery.com/jquery.ajax/
 * @param {String} interactiveObjectId ID interktivního objektu na stránce, který
 * spouští ajax volání. Toto ID musí být stabilní a unikátní v rámci zobrazené
 * webové stránky.
 * @returns {Boolean} <code>false</code>
 */
LT.ajax.send = function (params, interactiveObjectId) {
	// nastavi priznak, jestli se ma zamezit vicenasobnemu odesilani dat
	// pred ziskanim odpovedi ze serveru
	const preventResubmitBeforeFinish = LT.isTrue(interactiveObjectId);
	// ID requestu pouze pro ucely logovani
	const requestCode = LT.Suppliers.memoize(function () {
		let url = params["url"];
		let restUriBegin = LT.Optional
				.of(url.lastIndexOf("/"))
				.filter(function (index) {
					return index !== -1;
				})
				.map(function (index) {
					return index + 1;
				})
				.orElse(0);
		let restUriEnd = LT.Optional
				.of(url.indexOf("?"))
				.filter(function (index) {
					return index !== -1;
				})
				.orElse(url.length);
		let restUri = url.substring(restUriBegin, restUriEnd);
		return restUri + "_" + LT.randomString(5);
	});
	// osetrni vicenasobneho odesilani dat
	if (preventResubmitBeforeFinish) {
		// unikatni interactiveObjectId pouziju jako flag
		if (LT.flags.getThenSetTrue(interactiveObjectId)) {
			// submit na tomto objektu se stale jeste provadi
			LT.ajax.logState(requestCode, "LOCK", "cannot acquire lock \"" + interactiveObjectId + "\"");
			LT.log.warn("Volani LT.ajax.send() pred dokoncenim predchoziho pozadavku pro objekt [" + interactiveObjectId + "].");
			// umoznim reakci na tento stav pomoci callbacku
			let onRejectCallHandler = params["onReject"];
			if (onRejectCallHandler) {
				onRejectCallHandler(interactiveObjectId, "notfinished");
			}
			LT.ajax.logState(requestCode, "TERMINATED");
			return false;
		}
	}
	// log
	LT.ajax.logState(requestCode, "LOCK", preventResubmitBeforeFinish
			? "\"" + interactiveObjectId + "\" lock acquired"
			: "disabled");
	LT.ajax.logState(requestCode, "SETUP_URL", params["url"]);
	// default request headers
	var requestHeaders = {
		"Accept": "application/json",
		"Content-Type": "application/json"
	};
	// callback helpery
	var callbacks = {
		onSuccess: undefined,
		onError: undefined,
		onTimeout: undefined,
		onAbort: undefined,
		onParseError: undefined,
		onBefore: undefined,
		onComplete: undefined
	};
	for (var key in callbacks) {
		if (params[key]) {
			callbacks[key] = params[key];
			delete params[key];
			// log
			LT.ajax.logState(requestCode, "SETUP_CALLBACK", key);
		}
	}
	// log
	LT.ajax.logState(requestCode, "SETUP_DATA", params["data"]);
	// rozsireni parametru
	params = $.extend({
		type: params["method"] || "POST",
		cache: params["cache"] || false,
		timeout: params["timeout"] || 10000,
		beforeSend: function (xhr) {
			let customHeaders = params["requestHeaders"];
			for (var headerName in requestHeaders) {
				if (customHeaders && customHeaders[headerName]) {
					// prepsani defaultni hodnoty hlavicky
					xhr.setRequestHeader(headerName, customHeaders[headerName]);
					delete customHeaders[headerName];
				} else {
					// pouziti defaultni hodnoty
					if (headerName === "Content-Type") {
						if (params["contentType"] !== false) {
							xhr.setRequestHeader(headerName, requestHeaders[headerName]);
						}
					} else {
						xhr.setRequestHeader(headerName, requestHeaders[headerName]);
					}
				}
			}
			// nastaveni zbylych hlavicek
			if (customHeaders) {
				for (var headerName in customHeaders) {
					xhr.setRequestHeader(headerName, customHeaders[headerName]);
				}
			}
			// log
			LT.ajax.logState(requestCode, "BEFORE_SEND", xhr);
			// callback
			if (callbacks.onBefore) {
				callbacks.onBefore(xhr);
			}
		},
		complete: function (xhr, textStatus) {
			if (preventResubmitBeforeFinish) {
				// povoleni submitu dat na tomto objektu
				LT.flags.getThenSetFalse(interactiveObjectId);
				LT.ajax.logState(requestCode, "LOCK", "\"" + interactiveObjectId + "\" lock released");
			}
			// log
			LT.ajax.logState(requestCode, "FINISHED", xhr);
			// callback
			if (callbacks.onComplete) {
				callbacks.onComplete(xhr, textStatus);
			}
		},
		success: function (data, textStatus, xhr) {
			// log
			LT.ajax.logState(requestCode, "SUCCESS", xhr);
			LT.ajax.logState(requestCode, "RESPONSE", data, false);
			// callback
			if (callbacks.onSuccess) {
				callbacks.onSuccess(data, xhr, textStatus);
			}
		},
		error: function (xhr, textStatus, errorThrown) {
			// log
			LT.ajax.logState(requestCode, textStatus.toUpperCase(), xhr, false);
			// callbacks
			switch (textStatus) {
				case "timeout":
					// pozadavek nebylo mozne odeslat - server neodpovedel v pozadovanem case
					if (callbacks.onTimeout) {
						callbacks.onTimeout();
					} else {
						alert("Server neodpovídá, zkuste požadavek odeslat znovu.");
					}
					break;
				case "error":
					// nastala chyba pri zpracovani pozadavku
					if (callbacks.onError) {
						callbacks.onError(xhr.status, xhr.responseJSON);
					} else {
						alert("Při zpracování požadavku nastala chyba.");
					}
					break;
				case "abort":
					// odeslani pozadavku bylo zruseno
					if (callbacks.onAbort) {
						callbacks.onAbort();
					}
					break;
				case "parsererror":
					// odpovede serveru neobsahuje validni odpoved a nelze zpracovat
					if (callbacks.onParseError) {
						callbacks.onParseError();
					} else {
						alert("Při zpracování příchozího požadavku nastala chyba.");
					}
					break;
			}
		},
		statusCode: $.extend({
			// 200: function(xhr) {},
			// 405: function(xhr) {}
			// 500: function(xhr) {}
		}, params.statusCode || {})
	}, params);

	// log
	LT.ajax.logState(requestCode, "SETUP_METHOD", params["type"]);
	LT.ajax.logState(requestCode, "SETUP_TIMEOUT", params["timeout"] + " millis");
	LT.ajax.logState(requestCode, "SEND_REQUEST");

	$.ajax(params);

	return false;
};

/**
 * Odesle formular jako Ajax request.
 *
 * @param {HTMLElement|jQuery|String} form Selektor nebo objekt formulare.
 * @param {Object} params Nepovinny parametr, obsahuje callback funkce.
 */
LT.ajax.sendForm = function (form, params) {
	LT.checkNotNull(form, "Form must not be null");

	let formJQ = LT.isJQueryObject(form) ? form : $(form);
	let selectedElementsCount = formJQ.length;
	LT.checkArgument(selectedElementsCount === 1, "Selektor musi identifikovat prave jeden element form");

	let options = params || {};
	let htmlFormElement = formJQ.get(0);
	let formData = new FormData(htmlFormElement);

	// rozsireni parametru
	options = $.extend(
			{
				data: formData,
				contentType: false,
				processData: false,
				method: "POST",
				requestHeaders: {"Content-Type": "application/x-www-form-urlencoded"}
			}, options);

	// ziskam unikatni ID formulare
	const formId = LT.Optional
			.ofNullable(formJQ.data("interactive-object-id"))
			.orElse(formJQ.prop("id"));

	// odeslu formular na server
	LT.ajax.send(options, formId);
	return false;
};

/**
 * Ulozi do logu informace k volani LT.ajax.send() a LT.ajax.sendForm().
 *
 * @param {String|Number|Function} code Unikatni identifikator (pripadne
 * jeho supplier) konkretniho volani.
 * @param {String} state Popis (mezi)stavu, ve kterem se volani nachazi.
 * @param {Object} value Hodnota logovaneho stavu.
 */
LT.ajax.logState = function (code, state, value, stringify) {
	// kontrola vstupu
	LT.checkNotNull(code, "Request code must not be null.");
	LT.checkArgument(LT.isNotEmpty(state), "Request state must not be empty.");

	// supplier nebo hodnota
	let _code = LT.isFunction(code) ? code() : code;

	/** @type LT.Register */
	let ajaxLogCache = LT.cache.getPrivateCache("ajax");

	/** @type Array */
	let requestLog = ajaxLogCache.get(_code);
	if (LT.isUndefined(requestLog) || LT.isNull(requestLog)) {
		// soukroma cache zatim neexistuje, tak ji zalozim
		requestLog = new Array();
		ajaxLogCache.put(_code, requestLog);
		LT.log.debug("Provadim volani AJAX s kodem [" + _code + "]");
	}

	let _value = value;
	let valueType;
	if (LT.isNotUndefined(value) && LT.isNotNull(value)) {
		if (LT.isString(value)) {
			valueType = "string";
		} else if (LT.isArray(value)) {
			valueType = "array";
		} else if (LT.isDomElement(value)) {
			valueType = "element";
		} else if (LT.isJQueryObject(value)) {
			valueType = "jQuery";
		} else if (value instanceof FormData) {
			valueType = "FormData";
			_value = {};
			let entries = value.entries();
			let entry = entries.next();
			while (entry.done === false) {
				let fieldName = entry.value[0];
				// store field value
				_value[fieldName] = entry.value[1];
				// get next entry
				entry = entries.next();
			}
		} else {
			valueType = "object";
			if (stringify !== false) {
				_value = JSON.stringify(value);
			}
		}
	}
	let now = new Date();
	requestLog.push({
		"time": now.toLocaleTimeString() + " " + now.getMilliseconds(),
		"state": state,
		"value": _value,
		"valueType": valueType
	});
};

/**
 * Do konzole vypise zaznamy o provedenem lolani LT.ajax.send() nebo
 * LT.ajax.sendForm().
 *
 * @param {String|Number|Function} code Unikatni identifikator konkretniho
 * volani nebo null.
 */
LT.ajax.showLog = function (code) {
	/** @type LT.Register */
	let ajaxLogCache = LT.cache.getPrivateCache("ajax");

	// null, undefined, supplier nebo hodnota
	let _code = LT.isEmpty(code) ? null : (LT.isFunction(code) ? code() : code);

	let doShowLog = function (requestCode) {
		LT.log.info("AJAX log [" + requestCode + "]");
		let requestLog = ajaxLogCache.get(requestCode);

		if (LT.isEmpty(requestLog)) {
			console.log("#0 \t Zadne zaznamy");
			return;
		}

		for (var idx = 0; idx < requestLog.length; idx++) {
			let logRecord = requestLog[idx];
			let msg = "#" + (idx + 1) + "\t" + logRecord.time + "\t[" + logRecord.state + "]";
			let value = logRecord.value;
			let valueType = logRecord.valueType;
			if (LT.isNotEmpty(value)) {
				if (valueType === "string") {
					console.log(msg + "\t" + value);
				} else {
					console.log(msg);
					console.log(value);
				}
			} else {
				console.log(msg);
			}
		}
	};

	if (_code === null) {
		// zobrazim zaznamy vsech AJAX volani
		let keysArray = ajaxLogCache.getKeys();
		for (let i in keysArray) {
			let key = keysArray[i];
			doShowLog(key);
		}
	} else {
		// zobrazim zaznamy konkretniho AJAX volani
		doShowLog(_code);
	}
};

/*
 *	DOM HELPER FUNCTIONS
 */

/**
 * Zalozeni namespace pro DOM
 */
LT.dom = LT.dom || {};

/**
 * Prida elementu jednu nebo vice CSS trid. Pokud uz element pridavanou tridu
 * ma, tak se trida preskoci a pokaracuje se pridanim dalsi tridy v poradi.
 * <p>
 * Pouziti:</p>
 * <pre>
 * let element = document.getElementById("unique");
 *
 * LT.dom.addClass(element, "cls");
 *
 * LT.dom.addClass(element, "c1 c2 c3");
 *
 * LT.dom.addClass(element, "c1", "c2", ...);
 *
 * let arr = ["c1", "c2", ...];
 * LT.dom.addClass(element, arr);</pre>
 *
 * @param {DOMElement} domElement
 * @param {String|Array} classNames
 */
LT.dom.addClass = function () {
	const args = Array.prototype.slice.call(arguments);
	let domElement = args[0];
	let classNames = args.slice(1);
	LT.checkNotNull(domElement, "DOM element object must not be null.");
	LT.checkArgument(
			classNames.length !== 0,
			"CSS class name(s) must be defined."
			);
	//
	if (domElement.classList) {
		let _classNames =
				classNames.length === 1 &&
				typeof classNames[0] === "object" &&
				classNames[0] instanceof Array
				? classNames[0]
				: classNames;
		for (let clsIdx = 0; clsIdx < _classNames.length; clsIdx++) {
			let classesToAdd = _classNames[clsIdx].split(" ");
			for (let i = 0; i < classesToAdd.length; i++) {
				let singleClassName = classesToAdd[i];
				if (LT.isTrue(singleClassName)) {
					domElement.classList.add(singleClassName);
				}
			}
		}
	} else {
		// For IE9 and earlier
		let currentClasses = domElement.className.split(" ");
		for (var clsIdx = 0; clsIdx < classNames.length; clsIdx++) {
			let _classNames = classNames[clsIdx];
			let classesToAdd =
					typeof _classNames === "object" && _classNames instanceof Array
					? _classNames
					: _classNames.split(" ");
			for (let i = 0; i < classesToAdd.length; i++) {
				let singleClassName = classesToAdd[i];
				if (
						LT.isTrue(singleClassName) &&
						currentClasses.indexOf(singleClassName) === -1
						) {
					domElement.className += " " + singleClassName;
					currentClasses.push(singleClassName);
				}
			}
		}
	}
};

/**
 * Odebere elementu jednu nebo vice CSS trid. Pokud element odebidanou tridu
 * nema, tak se trida preskoci a pokaracuje se odebiranim dalsi tridy v poradi.
 * <p>
 * Pouziti:</p>
 * <pre>
 * let element = document.getElementById("unique");
 *
 * LT.dom.removeClass(element, "cls");
 *
 * LT.dom.removeClass(element, "c1 c2 c3");
 *
 * LT.dom.removeClass(element, "c1", "c2", ...);
 *
 * let arr = ["c1", "c2", ...];
 * LT.dom.removeClass(element, arr);</pre>
 *
 * @param {DOMElement} domElement
 * @param {String|Array} classNames
 */
LT.dom.removeClass = function () {
	const args = Array.prototype.slice.call(arguments);
	let domElement = args[0];
	let classNames = args.slice(1);
	LT.checkNotNull(domElement, "DOM element object must not be null.");
	LT.checkArgument(
			classNames.length !== 0,
			"CSS class name(s) must be defined."
			);
	//
	if (domElement.classList) {
		let _classNames =
				classNames.length === 1 &&
				typeof classNames[0] === "object" &&
				classNames[0] instanceof Array
				? classNames[0]
				: classNames;
		for (let clsIdx = 0; clsIdx < _classNames.length; clsIdx++) {
			let classesToAdd = _classNames[clsIdx].split(" ");
			for (let i = 0; i < classesToAdd.length; i++) {
				let singleClassName = classesToAdd[i];
				if (LT.isTrue(singleClassName)) {
					domElement.classList.remove(singleClassName);
				}
			}
		}
	} else {
		// For IE9 and earlier
		for (var clsIdx = 0; clsIdx < classNames.length; clsIdx++) {
			let _classNames = classNames[clsIdx];
			let classesToRemove =
					typeof _classNames === "object" && _classNames instanceof Array
					? _classNames
					: _classNames.split(" ");
			for (let i = 0; i < classesToRemove.length; i++) {
				let singleClassName = classesToRemove[i];
				if (LT.isTrue(singleClassName)) {
					let expression = new RegExp("\\b\\s*" + singleClassName + "\\b", "g");
					domElement.className = domElement.className.replace(expression, "");
				}
			}
		}
	}
};

/**
 * Vrati <code>true</code>, pokud element ma nastavenou uvedenou CSS tridu.
 *
 * @param {DOMElement} domElement
 * @param {String} className
 * @returns {Boolean}
 */
LT.dom.hasClass = function (domElement, className) {
	LT.checkNotNull(domElement, "DOM element object must not be null.");
	LT.checkNotNull(className, "CSS class name must not be null.");
	LT.checkArgument(LT.isTrue(className), "CSS class name must not be empty.");
	if (domElement.classList) {
		return domElement.classList.contains(className);
	} else {
		// For IE9 and earlier
		let expression = new RegExp("\\b" + className + "\\b", "g");
		return expression.test(domElement.className);
	}
};

/**
 * Odstrani z dokumentu predany element a vrati <code>true</code>. Pokud element
 * nelze odstranit, tak neprovede nic a vrati </code>false</code>.
 *
 * @param {HTMLElement} element
 * @returns {Boolean}
 */
LT.dom.removeElement = function (element) {
	LT.checkArgument(LT.isDomElement(element), "Argument must be DOM element.");
	let parent = element.parentElement;
	if (parent !== null) {
		parent.removeChild(element);
		return true;
	}
	return false;
};

/**
 * Prepise v dokumentu predany <code>element</code> hodnotou <code>replacement</code>.
 * Pokud <code>replacement</code> je <code>null</code>, <code>undefined</code>,
 * <code>""</code> nebo prazdny <code>jQuery</code> objekt, tak je element
 * smazana a neni nicim nahrazen.
 * <p>
 * Pokud je predana funkce <code>showEffectFunction</code> provede se po nahrazeni
 * elementu <code>element</code> pozadovana animace. Funkce musi byt deklarovana
 * jako <code>function effect(jQuery object) { ... }</code>.</p>
 * <p>
 * Priklad pouziti:</p>
 * <pre>
 * let element = document.getElementById("639675761");
 * let replacement = $("section.articles div.row");
 *
 * LT.dom.replaceElement_(element, replacement, function (objectJQ) { objectJQ.fadeIn(); });
 * </pre>
 *
 * @param {HTMLElement} element
 * @param {HTMLElement|jQuery|String} replacement
 * @param {Function} showEffectFunction
 */
LT.dom.replaceElement = function (element, replacement, showEffectFunction) {
	LT.checkArgument(LT.isDomElement(element), "Argument must be DOM element.");
	LT.checkArgument(
			LT.isUndefined(showEffectFunction) ||
			LT.isNull(showEffectFunction) ||
			LT.isFunction(showEffectFunction),
			"Effect must be function."
			);
	// pokud je replacement prazdny
	if (
			LT.isUndefined(replacement) ||
			LT.isNull(replacement) ||
			replacement === ""
			) {
		LT.dom.removeElement(element);
	}

	// vytvorim funkci, ktera provede inicializaci jQuery objektu pro "show efekt"
	let initEffect = LT.Optional.ofNullable(showEffectFunction)
			// pokud je efekt nastaven na "no operation", tak se nema aplikovat
			.filter(function (showFn) {
				return showFn !== LT.function.noop;
			})
			// pokud je nastavena funkce efektu, vratim inicializacni funkci
			.map(function (showFn) {
				let fn = function (replacementJQ) {
					replacementJQ.hide();
				};
				return fn;
			})
			// pokud parametry nastaveny nejsou, vratim "no opretaion"
			.orElse(LT.function.noop);

	// funkce, ktera nad jQuery objektem provede pozadovany "show efekt"
	let applyEfect = LT.Optional.ofNullable(showEffectFunction)
			// pokud parametry nastaveny nejsou, vratim "no opretaion"
			.orElse(LT.function.noop);

	// pokud nahrazuji DOM Nodem, provedu jednoduche nahrazeni pres parenta
	if (LT.isDomNode(replacement)) {
		initEffect($(replacement));
		// replace
		element.parentElement.replaceChild(replacement, element);
		// effect
		applyEfect($(replacement));
		return;
	}
	// pokud nahrazuji jQuery objektem, tak zalezi, kolik elementu obsahuje
	if (LT.isJQueryObject(replacement)) {
		let selectedElementsCount = replacement.length;
		if (selectedElementsCount === 0) {
			LT.log.warn("Relpacement je prazdy jQuery objekt.");
			LT.dom.removeElement(element);
		} else {
			let fragment = document.createDocumentFragment();
			for (var i = 0; i < selectedElementsCount; i++) {
				fragment.appendChild(replacement[i]);
			}
			initEffect(replacement);
			// replace
			element.parentElement.replaceChild(fragment, element);
			// effect
			applyEfect(replacement);
		}
		return;
	}
	// pokud nahrazuji HTML kodem v retezci, tak zajistim, aby byl spravne interpretovan
	if (LT.isString(replacement)) {
		let fragment = document.createDocumentFragment();
		{
			// DocumentFragment neposkytuje atribut innerHTML, proto se musi
			// pouzit pomocny element
			let tempElement = document.createElement("div");
			tempElement.innerHTML = replacement;
			initEffect($(tempElement).children());
			while (tempElement.firstChild) {
				var child = tempElement.removeChild(tempElement.firstChild);
				fragment.appendChild(child);
			}
		}
		let parentJQ = $(element.parentElement);
		// replace
		element.parentNode.replaceChild(fragment, element);
		// effect
		applyEfect(parentJQ.children());
	}
};

/**
 * Do predaneho elementu <code>element</code> vlozi hodnotou <code>content</code>,
 * tim prepise veskery puvodni obsah elementu. Pokud <code>content</code> je
 * <code>null</code>, <code>undefined</code>, <code>""</code> nebo prazdny
 * <code>jQuery</code> objekt, tak je obsah elementu smazan a jeho obsah bude
 * prazdny.
 *
 * @param {HTMLElement} element
 * @param {HTMLElement|jQuery|String} content
 */
LT.dom.insertIntoElement = function (element, content, effectIgnored) {
	LT.checkArgument(LT.isDomElement(element), "Argument must be DOM element.");
	// nejprve smazu obsah elementu
	element.innerHTML = "";
	LT.dom.insertAsLastChild(element, content);
};

/**
 * Do elementu <code>element</code> vlozi hodnotou <code>content</code>
 * jako first child. TODO revize: Pokud <code>content</code> je
 * <code>null</code>, <code>undefined</code>, <code>""</code> nebo prazdny
 * <code>jQuery</code> objekt, tak je obsah elementu smazan a jeho obsah bude
 * prazdny.
 *
 * @param {HTMLElement} element
 * @param {HTMLElement|jQuery|String} content
 */
LT.dom.insertAsFirstChild = function (element, content, effectIgnored) {
	LT.checkArgument(LT.isDomElement(element), "Argument must be DOM element.");
	// pokud je content prazdny, tak koncim
	if (LT.isUndefined(content) || LT.isNull(content) || content === "") {
		return;
	}
	// pokud vkladam DOM Node
	if (LT.isDomNode(content)) {
		if (element.childElementCount > 0) {
			element.insertBefore(content, element.firstChild);
		} else {
			element.appendChild(content);
		}
		return;
	}
	// pokud vkladam jQuery objekt, tak zalezi, kolik elementu obsahuje
	if (LT.isJQueryObject(content)) {
		let selectedElementsCount = content.length;
		if (selectedElementsCount === 0) {
			LT.log.warn("Vkladani prazdneho jQuery objektu.");
		} else {
			let fragment = document.createDocumentFragment();
			for (var i = 0; i < selectedElementsCount; i++) {
				fragment.appendChild(content[i]);
			}
			if (element.childElementCount > 0) {
				element.insertBefore(fragment, element.firstChild);
			} else {
				element.appendChild(fragment);
			}
		}
		return;
	}
	// pokud nahrazuji HTML kodem v retezci, tak zajistim, aby byl spravne interpretovan
	if (LT.isString(content)) {
		var temp = document.createElement('template');
		temp.innerHTML = content;
		if (element.childElementCount > 0) {
			element.insertBefore(temp.content, element.firstChild);
		} else {
			element.appendChild(temp.content);
		}
	}
};

/**
 * TODO - Do predaneho elementu <code>element</code> vlozi hodnotou <code>content</code>,
 * tim prepise veskery puvodni obsah elementu. Pokud <code>content</code> je
 * <code>null</code>, <code>undefined</code>, <code>""</code> nebo prazdny
 * <code>jQuery</code> objekt, tak je obsah elementu smazan a jeho obsah bude
 * prazdny.
 *
 * @param {HTMLElement} element
 * @param {HTMLElement|jQuery|String} content
 */
LT.dom.insertAsLastChild = function (element, content, effectIgnored) {
	LT.checkArgument(LT.isDomElement(element), "Argument must be DOM element.");
	// pokud je content prazdny, tak koncim
	if (LT.isUndefined(content) || LT.isNull(content) || content === "") {
		return;
	}
	// pokud vkladam DOM Node
	if (LT.isDomNode(content)) {
		element.appendChild(content);
		return;
	}
	// pokud vkladam jQuery objekt, tak zalezi, kolik elementu obsahuje
	if (LT.isJQueryObject(content)) {
		let selectedElementsCount = content.length;
		if (selectedElementsCount === 0) {
			LT.log.warn("Vkladani prazdneho jQuery objektu.");
		} else {
			let fragment = document.createDocumentFragment();
			for (var i = 0; i < selectedElementsCount; i++) {
				fragment.appendChild(content[i]);
			}
			element.appendChild(fragment);
		}
		return;
	}
	// pokud nahrazuji HTML kodem v retezci, tak zajistim, aby byl spravne interpretovan
	if (LT.isString(content)) {
		var temp = document.createElement('template');
		temp.innerHTML = content;
		element.appendChild(temp.content);
	}
};
