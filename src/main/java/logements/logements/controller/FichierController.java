package logements.logements.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fichiers")
public class FichierController {

    private static final List<String> TYPES_AUTORISES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final Path repertoireUpload;

    public FichierController(@Value("${app.upload.dir}") String repertoire) {
        this.repertoireUpload = Paths.get(repertoire).toAbsolutePath().normalize();
    }

    @PostMapping("/upload")
    public Map<String, String> televerser(@RequestParam("fichier") MultipartFile fichier) {
        if (fichier.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier est vide");
        }
        if (!TYPES_AUTORISES.contains(fichier.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seules les images (jpeg, png, webp, gif) sont autorisées");
        }

        try {
            Files.createDirectories(repertoireUpload);

            String extension = extraireExtension(fichier.getOriginalFilename());
            String nomFichier = UUID.randomUUID() + extension;
            Path destination = repertoireUpload.resolve(nomFichier).normalize();

            if (!destination.startsWith(repertoireUpload)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
            }

            Files.copy(fichier.getInputStream(), destination);

            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(nomFichier)
                    .toUriString();

            return Map.of("url", url);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec de l'enregistrement du fichier");
        }
    }

    private String extraireExtension(String nomOriginal) {
        if (nomOriginal == null || !nomOriginal.contains(".")) {
            return "";
        }
        return nomOriginal.substring(nomOriginal.lastIndexOf('.'));
    }
}
