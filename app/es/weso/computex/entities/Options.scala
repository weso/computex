package es.weso.computex.entities

import es.weso.utils.JenaUtils
import es.weso.computex.profile.Profile

case class Options(
    val profile:		Profile = Profile.Computex,
    val contentFormat:	String  = JenaUtils.TTL,
    val verbose: 		Boolean = false,
    val showSource:		Boolean = false,
    val expand:			Boolean = true,
 //   val imports:		Boolean = true,
    val prefix:			Boolean = true
    )

