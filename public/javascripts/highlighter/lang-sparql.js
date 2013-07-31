// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.



/**
 * @fileoverview
 * Registers a language handler for SPARQL.
 *
 *
 * To use, include prettify.js and this file in your HTML page.
 * Then put your code in an HTML tag like
 *      <pre class="prettyprint lang-sql">(my SQL code)</pre>
 *
 *
 * http://savage.net.au/SQL/sql-99.bnf.html is the basis for the grammar, and
 * http://msdn.microsoft.com/en-us/library/aa238507(SQL.80).aspx and
 * http://meta.stackoverflow.com/q/92352/137403 as the bases for the keyword
 * list.
 *
 * @author mikesamuel@gmail.com
 */

PR['registerLangHandler'](
    PR['createSimpleLexer'](
        [
         // Whitespace
         [PR['PR_PLAIN'],       /^[\t\n\r \xA0]+/, null, '\t\n\r \xA0'],
         // A double or single quoted, possibly multi-line, string.
         [PR['PR_STRING'],      /^(?:"(?:[^\"\\]|\\.)*"|'(?:[^\'\\]|\\.)*')/, null,
          '"\'']
        ],
        [
         // A comment is either a line comment that starts with two dashes, or
         // two dashes preceding a long bracketed block.
         [PR['PR_COMMENT'], /^[\s\t\n\r]#[^\r\n]*|\/\*[\s\S]*?(?:\*\/|$)/],
         [PR['PR_KEYWORD'], /^(?:AND|AS|ASC|BY|CONTAINS|CREATE|DELETE|DESC|DISTINCT|EXISTS|FROM|GROUP|HAVING|INSERT|INTERSECT|INTO|IS|NOT|NULL|OFFSET|OPTIONAL|OR|PREFIX|SELECT|VALUES|WHERE|CONSTRUCT|FILTER|UPDATE|NAMED|GRAPH|ORDER|ORDER|LIMIT|REDUCED|ASK|DESCRIBE|BIND|UNDEF|MINUS|GROUP_CONCAT|SEPARATOR)(?=[^\w-]|$)/i, null],
         // A number is a hex integer literal, a decimal real literal, or in
         // scientific notation.
         [PR['PR_LITERAL'],
          /^[+-]?(?:0x[\da-f]+|(?:(?:\.\d+|\d+(?:\.\d*)?)(?:e[+\-]?\d+)?))/i],
         // An identifier
         [PR['PR_PLAIN'], /^(\_\:[a-z_][\w-]*)|([?][a-z_][\w-]*)/],
		 [PR['PR_ATTRIB_VALUE'], /^[a-z_][\w-]*\:([a-z_A-Z][\w-]*)*/],
		 [PR['PR_ATTRIB_NAME'], /^[?]?[a-zA-Z_][\w-]*[:]?/],
		 [PR['PR_DECLARATION'], /^<http:[/][/][a-zA-z0-9\.\/\-#]*>/],
         // A run of punctuation
         [PR['PR_PUNCTUATION'], /^([^\w\t\n\r \xA0\"\'\:\?\<\>\/\.\#])|([\s\t\n\r]*\.)/]
        ]),
    ['sparql']);
