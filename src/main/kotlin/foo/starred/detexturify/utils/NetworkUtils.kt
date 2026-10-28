package foo.starred.detexturify.utils

import foo.starred.detexturify.Detexturify
import foo.starred.kontext.api.network.WebUtils

object NetworkUtils : WebUtils(Detexturify.modName, Detexturify.LOGGER)
