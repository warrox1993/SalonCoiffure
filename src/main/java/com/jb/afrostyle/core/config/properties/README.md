# Configuration Properties - Migration BusinessConstants

## Vue d'ensemble

Ce package contient l'externalisation complète des constantes BusinessConstants vers un système de configuration externe basé sur `@ConfigurationProperties` et Spring Boot.

## Architecture

### Avant la migration
```java
public final class BusinessConstants {
    public static final LocalTime BUSINESS_OPEN_TIME = LocalTime.of(8, 0);
    public static final int MAX_SERVICES_PER_BOOKING = 5;
    // ... 200+ constantes hardcodées
}
```

### Après la migration
```yaml
# application.yml
afrostyle:
  business:
    hours:
      open-time: "08:00"
      close-time: "20:00"
  booking:
    max-services-per-booking: 5
```

```java
@ConfigurationProperties(prefix = "afrostyle.business.hours")
public record BusinessHoursProperties(
    LocalTime openTime,
    LocalTime closeTime
) {}
```

## Structure des classes

### Classes de propriétés (Records Java 21)

- **`AfroStyleProperties`** - Configuration centrale regroupant toutes les propriétés
- **`BusinessHoursProperties`** - Heures d'ouverture et créneaux
- **`BookingProperties`** - Règles de réservation
- **`PaymentProperties`** - Configuration des paiements et devises
- **`UserProperties`** - Contraintes utilisateur (mot de passe, profil)
- **`SalonProperties`** - Configuration des salons
- **`NotificationProperties`** - Paramètres de notification
- **`PaginationProperties`** - Configuration de pagination
- **`CacheProperties`** - Durées de cache
- **`ValidationProperties`** - Règles de validation

### Configuration

- **`BusinessPropertiesConfig`** - Active toutes les @ConfigurationProperties
- **`AfroStyleMonolithApplication`** - Point d'entrée avec @EnableConfigurationProperties

## Validation automatique

Chaque classe de propriétés utilise Bean Validation pour garantir des valeurs valides :

```java
public record BookingProperties(
    @Min(value = 15, message = "Durée minimum: 15 minutes")
    @Max(value = 60, message = "Durée maximum: 60 minutes") 
    int minDurationMinutes,
    
    @NotEmpty(message = "Devises requises")
    Set<@Pattern(regexp = "^[A-Z]{3}$") String> supportedCurrencies
) {
    @ConstructorBinding
    public BookingProperties {
        // Validation personnalisée cross-field
        if (minDurationMinutes >= maxDurationMinutes) {
            throw new IllegalArgumentException("Durée min < max");
        }
    }
}
```

## Configuration par profil

Le système supporte différents profils avec des valeurs adaptées :

### Développement (`dev`)
```yaml
afrostyle:
  booking:
    max-services-per-booking: 5
    cancellation-deadline-hours: 24
```

### Production (`prod`) 
```yaml
afrostyle:
  booking:
    max-services-per-booking: 3  # Plus strict
    cancellation-deadline-hours: 48  # Plus strict
```

### Docker (`docker`)
```yaml
# Utilise les mêmes valeurs que dev mais avec différentes connexions DB
```

## Compatibilité ascendante

La classe `BusinessConstants` est maintenue comme facade/adapter :

```java
@Component
@Deprecated(since = "2.0", forRemoval = false)
public final class BusinessConstants {
    
    private static AfroStyleProperties properties;
    
    // Nouvelles méthodes recommandées
    public static LocalTime getBusinessOpenTime() {
        return properties.businessHours().openTime();
    }
    
    // Anciennes constantes deprecated
    @Deprecated(since = "2.0", forRemoval = true)
    public static final LocalTime BUSINESS_OPEN_TIME = LocalTime.of(8, 0);
}
```

## Usage recommandé

### Dans le code existant (compatible)
```java
// Fonctionne encore mais deprecated
int maxServices = BusinessConstants.MAX_SERVICES_PER_BOOKING;

// Nouvelle méthode recommandée  
int maxServices = BusinessConstants.getMaxServicesPerBooking();
```

### Dans le nouveau code (injection directe)
```java
@Service
public class BookingService {
    
    private final BookingProperties bookingProperties;
    
    public BookingService(BookingProperties bookingProperties) {
        this.bookingProperties = bookingProperties;
    }
    
    public void createBooking() {
        int maxServices = bookingProperties.maxServicesPerBooking();
        // ...
    }
}
```

## Tests

### Tests de propriétés
- **`AfroStylePropertiesTest`** - Validation des propriétés et contraintes
- **`BusinessConstantsIntegrationTest`** - Test d'intégration de la migration

### Configuration de test
```properties
# application-test.properties
afrostyle.booking.min-duration-minutes=15
afrostyle.payment.default-currency=EUR
# ... configuration minimale pour tests
```

## Avantages de cette architecture

### ✅ Type Safety
- Types forts avec records Java 21
- Validation à la compilation
- IDE autocompletion

### ✅ Validation automatique
- Bean Validation au démarrage
- Validation cross-field personnalisée
- Messages d'erreur explicites

### ✅ Externalisation complète
- Configuration dans application.yml
- Support des profils Spring
- Variables d'environnement supportées

### ✅ Immutabilité
- Records Java immuables
- Thread-safe par design
- Pas de modification runtime

### ✅ Testabilité
- Configuration mockable
- Tests d'intégration complets
- Validation indépendante

## Migration path

1. **Phase 1** ✅ - Créer les @ConfigurationProperties
2. **Phase 2** ✅ - Migrer application.yml  
3. **Phase 3** ✅ - Adapter BusinessConstants (facade)
4. **Phase 4** ✅ - Tests complets
5. **Phase 5** 🔄 - Migrer progressivement le code vers injection directe
6. **Phase 6** 📋 - Supprimer BusinessConstants deprecated (future)

## Notes importantes

- ⚠️ L'application ne démarre pas si une propriété est invalide
- ⚠️ Toutes les validations sont automatiques au démarrage
- ⚠️ Les profils prod ont des valeurs plus strictes que dev
- ✅ Zero breaking change - compatibilité 100% assurée
- ✅ Performance améliorée (pas de recalcul constant)
- ✅ Configuration centralisée et documentée