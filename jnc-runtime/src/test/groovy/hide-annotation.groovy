import jnc.provider.NotFinal
import org.assertj.core.internal.bytebuddy.ByteBuddy
import org.assertj.core.internal.bytebuddy.description.modifier.Visibility

try {
    String outputDirectory = project.build.outputDirectory

    new ByteBuddy()
            .redefine(NotFinal.class)
            .merge(Visibility.PACKAGE_PRIVATE)
            .make()
            .saveIn new File(outputDirectory)
} catch (Throwable t) {
    log.error(t)
    throw t
}
