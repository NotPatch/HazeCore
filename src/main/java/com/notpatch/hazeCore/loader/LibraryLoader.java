package com.notpatch.hazeCore.loader;

import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.util.NLogger;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import lombok.NoArgsConstructor;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class LibraryLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolveLibrariesFromYml(classpathBuilder).stream()
                .map(DefaultArtifact::new)
                .forEach(artifact -> resolver.addDependency(new Dependency(artifact, null)));

        resolver.addRepository(new RemoteRepository.Builder("central", "default", getMavenUrl()).build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack.io", "default", "https://jitpack.io").build());

        classpathBuilder.addLibrary(resolver);
    }

    @NotNull
    private List<String> resolveLibrariesFromYml(@NotNull PluginClasspathBuilder classpathBuilder) {
        try (InputStream inputStream = getLibraryListFile()) {
            if (inputStream == null) {
                NLogger.warn("paper-libraries.yml bulunamadı, kütüphane yüklenmeyecek.");
                return List.of();
            }

            Yaml yaml = new Yaml();
            Map<String, List<String>> data = yaml.load(inputStream);

            List<String> libraries = data.get("libraries");

            if (libraries == null || libraries.isEmpty()) {
                NLogger.info("paper-libraries.yml içinde yüklenecek kütüphane bulunamadı.");
                return List.of();
            }

            return libraries;

        } catch (Exception e) {
            NLogger.error("paper-libraries.yml okunurken veya işlenirken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
        return List.of();
    }


    @Nullable
    private InputStream getLibraryListFile() {
        return HazeCore.class.getClassLoader().getResourceAsStream("paper-libraries.yml");
    }


    @NotNull
    private String getMavenUrl() {
        return Stream.of(
                System.getenv("PAPER_DEFAULT_CENTRAL_REPOSITORY"),
                "https://repo.maven.apache.org/maven2/"
        ).filter(Objects::nonNull).findFirst().orElseThrow();
    }
}

