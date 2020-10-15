import org.gradle.caching.configuration.AbstractBuildCache
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

rootProject.name = "custom-build-cache-issue"

buildCache {
    registerBuildCacheService(CustomBuildCache::class.java, CustomBuildCacheFactory::class.java)
    remote<CustomBuildCache> {
        path = "cache"
    }
}

open class CustomBuildCache constructor(
        var path: String? = null
): AbstractBuildCache() {
    constructor(): this("")
}

open class CustomBuildCacheFactory : BuildCacheServiceFactory<CustomBuildCache> {
    override fun createBuildCacheService(
            config: CustomBuildCache, describer: BuildCacheServiceFactory.Describer): BuildCacheService {
        return CustomBuildCacheService(File(config.path!!))
    }
}

open class CustomBuildCacheService(private val path: File) : BuildCacheService {
    override fun store(key: BuildCacheKey, writer: BuildCacheEntryWriter) {
        path.resolve(key.hashCode).outputStream().use {
            writer.writeTo(it)
        }
    }

    override fun close() {
    }

    override fun load(key: BuildCacheKey, reader: BuildCacheEntryReader): Boolean {
        if (!path.resolve(key.hashCode).exists()) return false

        path.resolve(key.hashCode).inputStream().use {
            reader.readFrom(it)
        }
        return true
    }

}
