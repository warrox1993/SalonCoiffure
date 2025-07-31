# 🧠 ARCHITECTURE CORE - CERVEAU AFROSTYLE MONOLITH

## Vue d'ensemble

Le package `/core` centralise 100% des composants partagés pour éliminer toute redondance et créer un "cerveau" architectural unifié.

## Structure Complète

```
src/main/java/com/jb/afrostyle/core/
├── constants/                    # 🔧 Constantes métier centralisées
│   ├── BusinessConstants.java    # Constantes business (heures, limites, etc.)
│   ├── SecurityConstants.java    # Constantes sécurité (JWT, sessions, etc.)
│   ├── ValidationConstants.java  # Constantes validation (regex, formats, etc.)
│   └── IntegrationConstants.java # Constantes intégrations (Stripe, Google, etc.)
│
├── dto/                          # 📦 DTOs/Records communs
│   ├── ApiResponse.java          # Déplacé de user/dto/
│   ├── UserResponse.java         # Déplacé de security/
│   ├── SessionAuthResponse.java  # Déplacé de security/
│   ├── ErrorResponse.java        # Déplacé de security/
│   └── PaginatedResponse.java    # Nouveau - pagination standardisée
│
├── enums/                        # 🏷️ Enums partagés
│   ├── EntityType.java           # Types d'entités pour exception handling
│   ├── Operation.java            # Types d'opérations CRUD
│   ├── AuthResult.java           # Résultats d'authentification
│   └── ValidationErrorType.java  # Types d'erreurs validation
│
├── exception/                    # 🚨 Exceptions centralisées
│   ├── ExceptionUtils.java       # Déplacé de util/
│   ├── BusinessException.java    # Exception métier générique
│   ├── ValidationException.java  # Exception validation
│   ├── AuthenticationException.java # Exception auth
│   ├── GlobalExceptionHandler.java # Handler global
│   └── specialized/              # Exceptions spécialisées
│       ├── UserException.java    # Déplacé de user/exception/
│       ├── SalonException.java   # Fusionné salon exceptions
│       ├── BookingException.java # Exception booking
│       └── PaymentException.java # Exception payment
│
├── mapper/                       # 🔄 Configuration MapStruct
│   ├── CoreMapperConfig.java     # Configuration MapStruct centralisée
│   ├── MapperRegistry.java       # Registre de tous les mappers
│   └── BaseMapper.java           # Interface mapper de base
│
├── response/                     # 📡 Factory de réponses HTTP
│   ├── ResponseFactory.java      # Factory intelligente principale
│   ├── ResponseUtils.java        # Déplacé de util/
│   ├── ResponseBuilder.java      # Builder Pattern pour réponses
│   └── HttpStatusResolver.java   # Résolution intelligente status HTTP
│
├── security/                     # 🔐 Sécurité centralisée
│   ├── CustomUserPrincipal.java  # Déplacé de user/security/
│   ├── AuthenticationUtil.java   # Déplacé de security/util/
│   ├── SecurityContext.java      # Context sécurité partagé
│   ├── PermissionChecker.java    # Vérification permissions
│   └── TokenProvider.java        # Provider JWT centralisé
│
├── util/                         # 🛠️ Utilitaires partagés
│   ├── ValidationUtils.java      # Déjà présent
│   ├── DateTimeUtils.java        # Utilitaires date/temps
│   ├── StringUtils.java          # Utilitaires chaînes
│   ├── CollectionUtils.java      # Utilitaires collections
│   ├── CryptoUtils.java          # Utilitaires cryptographie
│   └── JsonUtils.java            # Utilitaires JSON
│
├── validation/                   # ✅ Validations centralisées
│   ├── ValidationResult.java     # Déjà présent
│   ├── ValidatorRegistry.java    # Registre validateurs
│   ├── BusinessRuleValidator.java # Règles métier centralisées
│   ├── constraints/              # Contraintes custom
│   │   ├── ValidBusinessHours.java # Déplacé de booking/
│   │   ├── ValidCurrency.java    # Déplacé de payment/
│   │   ├── ValidPassword.java    # Déplacé de user/
│   │   └── ValidPhoneNumber.java # Déplacé de user/
│   └── validators/               # Implémentations
│       ├── BusinessHoursValidator.java
│       ├── CurrencyValidator.java
│       ├── PasswordValidator.java
│       └── PhoneNumberValidator.java
│
└── config/                       # ⚙️ Configurations centralisées
    ├── CoreConfig.java           # Configuration principale /core
    ├── ValidationConfig.java     # Configuration validation centralisée
    ├── MapperConfig.java         # Configuration MapStruct
    └── SecurityCoreConfig.java   # Configuration sécurité /core
```

## Bénéfices Architecture /core

### 🎯 **Centralisation Complète**
- **100% des composants partagés** dans /core
- **Zéro duplication** entre modules
- **Point d'entrée unique** pour chaque fonctionnalité

### 🚀 **Performance et Maintenabilité**
- **Singleton pattern** pour utilitaires
- **Lazy loading** des composants lourds
- **Cache intégré** pour validations fréquentes
- **Tests centralisés** dans /core/test/

### 🔧 **Développement Optimisé**
- **Auto-complétion IntelliSense** pour tous utilitaires
- **Documentation centralisée** avec JavaDoc complet
- **Standards uniformes** pour tous les modules
- **Refactoring simplifié** avec IDE support

### 📊 **Métriques Cibles**
- **Réduction 60%** des lignes de code répétitives
- **Élimination 100%** de la duplication inter-modules
- **Accélération 40%** du développement de nouvelles features
- **Amélioration 80%** de la maintenabilité du code

## Migration Strategy

### Phase 1: Structure et Foundation
1. Créer la structure /core complète
2. Implémenter les classes de base (ResponseFactory, etc.)
3. Définir les interfaces et contrats

### Phase 2: Migration Progressive
1. Déplacer les composants existants vers /core
2. Adapter les imports dans tous les modules
3. Vérifier la compilation à chaque étape

### Phase 3: Optimisation et Tests
1. Optimiser les performances des composants /core
2. Créer une suite de tests complète
3. Documentation finale et guides d'utilisation

## Standards de Code /core

- **Java 21** avec Pattern Matching avancé
- **Records** pour toutes les structures de données
- **Sealed interfaces** pour les hiérarchies fermées
- **Builder Pattern** pour les objets complexes
- **Annotation-based validation** avec Bean Validation
- **MapStruct** pour tous les mappings
- **SLF4J** pour le logging uniforme

Cette architecture /core transforme AfroStyleMonolith en une application moderne, maintenable et extensible avec un "cerveau" architectural centralisé.