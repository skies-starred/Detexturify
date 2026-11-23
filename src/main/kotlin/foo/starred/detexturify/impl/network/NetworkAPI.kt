package foo.starred.detexturify.impl.network

import foo.starred.detexturify.Detexturify
import foo.starred.kontext.api.network.WebUtils

object NetworkAPI : WebUtils(Detexturify.Data.name, Detexturify.LOGGER)
