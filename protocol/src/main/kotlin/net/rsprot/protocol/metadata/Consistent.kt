package net.rsprot.protocol.metadata

/**
 * This annotation is used to specify that a given packet encoder or decoder
 * does not get scrambled between revisions. It does not however guarantee that
 * changes to the packet itself are not performed - although these are rather rare.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
public annotation class Consistent
