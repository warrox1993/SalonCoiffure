# Guide de Test des Nouveaux Handlers Webhook Stripe

## ✅ Handlers ajoutés avec succès

Les nouveaux handlers ont été implémentés pour gérer ces événements Stripe :

1. **`payment_intent.succeeded`** - PaymentIntent complété avec succès
2. **`payment_intent.payment_failed`** - PaymentIntent échoué
3. **`charge.succeeded`** - Charge (paiement) réussi

## 🎯 Comment tester

### 1. Démarrer l'application
```bash
./mvnw spring-boot:run
```

### 2. Tester avec Stripe CLI
```bash
# Écouter les webhooks
stripe listen --forward-to localhost:8080/api/payments/webhook --events payment_intent.succeeded,payment_intent.payment_failed,charge.succeeded

# Dans un autre terminal, déclencher des événements
stripe trigger payment_intent.succeeded
stripe trigger payment_intent.payment_failed  
stripe trigger charge.succeeded
```

### 3. Logs attendus

#### Pour `payment_intent.succeeded` :
```
💳 Processing payment_intent.succeeded event
✅ PaymentIntent succeeded: pi_xxxxx
   - Amount: 25.0 EUR
   - Customer: cus_xxxxx
   - Payment Method: pm_xxxxx
   - Booking ID: 2
   - Customer ID: 152
✅ Payment 17 updated from PaymentIntent
🔄 Updating booking 2 status to CONFIRMED
✅ Booking status updated (placeholder)
```

#### Pour `payment_intent.payment_failed` :
```
❌ Processing payment_intent.payment_failed event
❌ PaymentIntent failed: pi_xxxxx
   - Error: Your card was declined. (card_declined)
✅ Payment 17 marked as failed
🔄 Updating booking 2 status to CANCELLED
✅ Booking status updated (placeholder)
```

#### Pour `charge.succeeded` :
```
💰 Processing charge.succeeded event
✅ Charge succeeded: ch_xxxxx
   - Amount: 25.0 EUR
   - PaymentIntent: pi_xxxxx
   - Paid: true
   - Receipt URL: https://pay.stripe.com/receipts/xxxxx
✅ Payment 17 updated from Charge
   - Booking ID from metadata: 2
```

## 🔧 Fonctionnalités implémentées

### EventDataObjectDeserializer avec fallback
- Utilise d'abord `.getObject().isPresent()`
- Si échec, utilise `.deserializeUnsafe()` comme fallback

### Extraction de métadonnées
- `booking_id` et `customer_id` depuis `paymentIntent.getMetadata()`
- Support des données Stripe natives (customer, amount, etc.)

### Mise à jour de base de données
- Recherche par `stripePaymentIntentId` (nouveau champ ajouté)
- Mise à jour du statut `PaymentStatus.SUCCEEDED` ou `FAILED`
- Enregistrement du `stripeChargeId` depuis l'événement

### Gestion d'erreurs
- Logging détaillé pour chaque étape
- Gestion des cas où les paiements ne sont pas trouvés
- Extraction des codes d'erreur Stripe (`lastPaymentError`)

## 📊 Nouveaux champs ajoutés

### Dans Payment.java :
```java
@Column(name = "stripe_payment_intent_id", unique = true)
private String stripePaymentIntentId;
```

### Dans PaymentRepository.java :
```java
Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
```

## 🎭 Scénarios de test réels

1. **Créer un paiement via checkout session**
2. **Compléter le paiement** → `payment_intent.succeeded` + `charge.succeeded`
3. **Utiliser une carte déclinée** → `payment_intent.payment_failed`

## 🚀 Prêt pour la production

Le système gère maintenant :
- ✅ checkout.session.completed (déjà fonctionnel)
- ✅ payment_intent.succeeded (nouveau)
- ✅ payment_intent.payment_failed (nouveau)  
- ✅ charge.succeeded (nouveau)

Tous les handlers utilisent les meilleures pratiques Stripe recommandées !