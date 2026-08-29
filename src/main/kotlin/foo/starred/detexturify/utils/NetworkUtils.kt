package foo.starred.detexturify.utils

import foo.starred.detexturify.Detexturify
import foo.starred.snowbird.api.network.WebUtils

object NetworkUtils : WebUtils(Detexturify.modName, Detexturify.LOGGER)
