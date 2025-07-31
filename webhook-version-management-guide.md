# Guide de Gestion des Versions Webhook Stripe

## 🎯 Vue d'ensemble

Ce système implémente la gestion avancée des versions d'API webhook Stripe selon les meilleures pratiques officielles, permettant des mises à jour sans temps d'arrêt (blue-green deployment).

## 🏗️ Architecture

### Composants principaux :

1. **WebhookVersionManager** - Gestion des versions et stratégies de migration
2. **WebhookEventService** - Idempotence et cache Redis des événements
3. **StripeWebhookController** - Endpoints avec support des versions
4. **RedisConfig** - Configuration pour l'idempotence

## 📋 Fonctionnalités

### ✅ Gestion des versions d'API
- Support des versions legacy et nouvelles simultanément
- Détection automatique de version via URL ou en-tête
- Stratégie blue-green pour migration sans downtime

### ✅ Idempotence des événements
- Cache Redis pour éviter les doublons
- Verrous distribués pour traitement concurrent
- TTL automatique pour nettoyage

### ✅ Monitoring et observabilité
- Métriques en temps réel
- Endpoints de santé dédiés
- Statistiques de traitement

## 🚀 Utilisation

### URLs webhook supportées :

```bash
# Version par défaut (legacy)
POST /api/payments/webhook

# Version spécifique
POST /api/payments/webhook?version=2024-06-20

# Version nouvelle
POST /api/payments/webhook?version=2025-06-30.basil
```

### Configuration des phases de migration :

```properties
# Phase 1: Mode normal (traiter seulement legacy)
stripe.webhook.migration.enabled=false
stripe.webhook.migration.process-legacy=true
stripe.webhook.migration.process-new=false

# Phase 2: Préparation migration (traiter legacy + ignorer new)
stripe.webhook.migration.enabled=true
stripe.webhook.migration.process-legacy=true
stripe.webhook.migration.process-new=false

# Phase 3: Migration active (traiter les deux)
stripe.webhook.migration.enabled=true
stripe.webhook.migration.process-legacy=true
stripe.webhook.migration.process-new=true

# Phase 4: Migration terminée (seulement new)
stripe.webhook.migration.enabled=true
stripe.webhook.migration.process-legacy=false
stripe.webhook.migration.process-new=true
```

## 🔄 Processus de migration étape par étape

### Étape 1: Préparation
```bash
# 1. Créer un nouvel endpoint webhook dans Stripe Dashboard
# URL: https://your-domain.com/api/payments/webhook?version=2025-06-30.basil
# 2. Le désactiver temporairement
# 3. Déployer le code avec migration.enabled=false
```

### Étape 2: Activation du dual-mode
```bash
# 1. Configurer pour ignorer les nouveaux événements
stripe.webhook.migration.enabled=true
stripe.webhook.migration.process-legacy=true
stripe.webhook.migration.process-new=false

# 2. Activer le nouvel endpoint Stripe
# 3. Surveiller les logs pour voir les événements ignorés
```

### Étape 3: Migration active
```bash
# 1. Activer le traitement des nouveaux événements
stripe.webhook.migration.process-new=true

# 2. Les deux versions sont maintenant traitées
# 3. Surveiller les métriques et erreurs
```

### Étape 4: Finalisation
```bash
# 1. Rejeter les anciens événements
stripe.webhook.migration.process-legacy=false

# 2. Désactiver l'ancien endpoint Stripe
# 3. Surveillance continue
```

## 📊 Endpoints de monitoring

### Statut de migration
```bash
GET /api/payments/webhook/migration-status
```

Réponse :
```json
{
  "migrationEnabled": true,
  "processLegacyEvents": false,
  "processNewEvents": true,
  "currentApiVersion": "2025-06-30.basil",
  "legacyApiVersion": "2024-09-30.acacia",
  "configuredApiVersion": "2025-06-30.basil",
  "phase": "COMPLETED"
}
```

### Statistiques des événements
```bash
GET /api/payments/webhook/stats
```

Réponse :
```json
{
  "processedEvents": 1250,
  "processingEvents": 2,
  "failedEvents": 15,
  "totalEvents": 1267,
  "successRate": 98.66,
  "failureRate": 1.18
}
```

### Santé des webhooks
```bash
GET /api/payments/webhook/health
```

Réponse :
```json
{
  "healthy": true,
  "totalEvents": 1267,
  "successRate": 98.66,
  "failureRate": 1.18,
  "migrationPhase": "COMPLETED",
  "currentVersion": "2025-06-30.basil",
  "status": "HEALTHY"
}
```

## 🛠️ Opérations de maintenance

### Débloquer un événement bloqué
```bash
POST /api/payments/webhook/release-lock/evt_1234567890
```

### Nettoyer les anciens événements
```bash
POST /api/payments/webhook/cleanup
```

## 📝 Logs de migration

### Phase préparation :
```
🔍 Version detection: apiVersion=2025-06-30.basil, versionParam=2025-06-30.basil, detected=2025-06-30.basil
⏳ Ignoring new event during preparation (version: 2025-06-30.basil)
```

### Phase migration :
```
🔍 Version detection: apiVersion=2024-09-30.acacia, versionParam=null, detected=2024-09-30.acacia
✅ Processing legacy event (version: 2024-09-30.acacia)
🆔 Event ID: evt_1234567890
🔒 Event evt_1234567890 marked as processing
✅ Event evt_1234567890 processed and marked as completed
```

### Phase finalisation :
```
🔍 Version detection: apiVersion=2024-09-30.acacia, versionParam=null, detected=2024-09-30.acacia
❌ Rejecting legacy event evt_1234567890 (version: 2024-09-30.acacia)
```

## 🚨 Gestion d'erreurs

### Codes de retour HTTP :

- **200 OK** : Événement traité avec succès ou ignoré volontairement
- **400 Bad Request** : Version legacy rejetée (permet retry automatique)
- **400 Bad Request** : Signature invalide (pas de retry)
- **500 Internal Server Error** : Erreur de traitement (retry possible)

### Stratégie de rollback :

1. **Arrêt d'urgence** : `stripe.webhook.migration.enabled=false`
2. **Retour à legacy** : `process-legacy=true, process-new=false`
3. **Réactivation ancien endpoint** dans Stripe Dashboard
4. **Surveillance des retries** automatiques

## 🔧 Configuration Redis

```properties
# Redis pour idempotence webhook
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0
spring.data.redis.timeout=2000ms
```

### Structure des clés Redis :
- `webhook:processed:{eventId}` - Événements traités (TTL: 24h)
- `webhook:processing:{eventId}` - Événements en cours (TTL: 10min)
- `webhook:failed:{eventId}` - Événements échoués (TTL: 24h)

## 🎯 Avantages de cette implémentation

1. **Zero downtime** : Migration sans interruption de service
2. **Idempotence** : Protection contre les doublons d'événements
3. **Observabilité** : Monitoring complet et métriques
4. **Rollback safe** : Retour arrière possible à tout moment
5. **Production ready** : Gestion d'erreurs robuste et logs détaillés

Cette implémentation suit exactement les recommandations officielles Stripe pour la gestion des versions webhook en environnement de production.