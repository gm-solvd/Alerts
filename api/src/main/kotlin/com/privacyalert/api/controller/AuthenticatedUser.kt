package com.privacyalert.api.controller

import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

fun authenticatedUserId(): UUID = SecurityContextHolder.getContext().authentication.principal as UUID
