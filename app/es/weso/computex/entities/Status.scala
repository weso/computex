package es.weso.computex.entities

sealed abstract class Status;
case object Valid 		extends Status;
case object Invalid 	extends Status;
case object Idle		extends Status;

