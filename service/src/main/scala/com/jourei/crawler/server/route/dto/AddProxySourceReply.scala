package com.jourei.crawler.server.route.dto

/**
 *
 * @param summary host, port
 */
final case class AddProxySourceReply(summary: Set[(String, String)])
