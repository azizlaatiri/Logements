package logements.logements.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String repertoireUpload;

    public WebConfig(@Value("${app.upload.dir}") String repertoireUpload) {
        this.repertoireUpload = repertoireUpload;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String emplacement = Paths.get(repertoireUpload).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(emplacement);
    }
}
