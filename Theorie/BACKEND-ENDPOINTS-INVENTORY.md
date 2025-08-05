# 📋 INVENTAIRE COMPLET DES ENDPOINTS BACKEND - AFROSTYLE MONOLITH

**Date**: 23 juillet 2025  
**Backend**: `C:\Users\jeanb\Desktop\AfroStyleMonolith`  
**Frontend**: `C:\Users\jeanb\Desktop\SalonBookingFront`  
**Status**: Backend en mode LECTURE SEULE - Connexion frontend en cours

---

## 📊 RÉSUMÉ DES ENDPOINTS

**Total Endpoints**: 83 endpoints (6 endpoints catégories exclus)  
**Modules**: 8 modules principaux (catégorie désactivée)  
**WebSocket**: 6 endpoints STOMP  

---

## 🔐 MODULE AUTHENTIFICATION (11 endpoints)

### AuthController - `/api/auth`

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | POST | `/api/auth/register` | Inscription utilisateur | Public | 🔴 CRITIQUE |
| ❓ | POST | `/api/auth/login` | Connexion (JsonAuthFilter) | Public | 🔴 CRITIQUE |
| ❓ | POST | `/api/auth/logout` | Déconnexion | Authentifié | 🔴 CRITIQUE |
| ❓ | POST | `/api/auth/forgot-password` | Mot de passe oublié | Public | 🟡 HAUTE |
| ❓ | POST | `/api/auth/reset-password` | Réinitialiser MDP | Public | 🟡 HAUTE |
| ❓ | POST | `/api/auth/change-password` | Changer MDP | Authentifié | 🟢 MOYENNE |
| ❓ | GET | `/api/auth/check-username/{username}` | Vérifier username | Public | 🟢 MOYENNE |
| ❓ | GET | `/api/auth/check-email/{email}` | Vérifier email | Public | 🟢 MOYENNE |
| ❓ | GET | `/api/auth/debug` | Debug auth | Public | 🔵 BASSE |
| ❓ | GET | `/api/auth/me` | Info utilisateur connecté | Authentifié | 🔴 CRITIQUE |

---

## 👤 MODULE UTILISATEUR (5 endpoints)

### UserController - `/api/users`

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | POST | `/api/users` | Créer utilisateur | ADMIN | 🔵 BASSE |
| ❓ | GET | `/api/users` | Lister utilisateurs | ADMIN | 🔵 BASSE |
| ❓ | GET | `/api/users/{userId}` | Obtenir utilisateur | Public | 🟡 HAUTE |
| ❓ | PUT | `/api/users/{id}` | Mettre à jour utilisateur | ADMIN/Owner | 🟡 HAUTE |
| ❓ | DELETE | `/api/users/{id}` | Supprimer utilisateur | ADMIN | 🔵 BASSE |

---

## 📅 MODULE RÉSERVATION (17 endpoints)

### BookingController - `/api/bookings` (9 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | POST | `/api/bookings` | Créer réservation | Authentifié | 🔴 CRITIQUE |
| ❓ | GET | `/api/bookings/{id}` | Obtenir réservation | Authentifié | 🔴 CRITIQUE |
| ❓ | GET | `/api/bookings/me` | Mes réservations | Authentifié | 🔴 CRITIQUE |
| ❓ | GET | `/api/bookings/salon` | Réservations salon | SALON_OWNER | 🟡 HAUTE |
| ❓ | PATCH | `/api/bookings/{id}/cancel` | Annuler réservation | Authentifié | 🔴 CRITIQUE |
| ❓ | PATCH | `/api/bookings/{id}/confirm` | Confirmer réservation | SALON_OWNER | 🟡 HAUTE |
| ❓ | PATCH | `/api/bookings/{id}/complete` | Terminer réservation | SALON_OWNER | 🟢 MOYENNE |
| ❓ | GET | `/api/bookings/salon/stats` | Stats salon | SALON_OWNER | 🟢 MOYENNE |
| ❓ | GET | `/api/bookings/slots/date/{date}` | Créneaux réservés | Public | 🔴 CRITIQUE |

### AvailabilityController - `/api/availability` (8 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | POST | `/api/availability` | Créer disponibilité | SALON_OWNER | 🟡 HAUTE |
| ❓ | PUT | `/api/availability/{id}` | Modifier disponibilité | SALON_OWNER | 🟡 HAUTE |
| ❓ | DELETE | `/api/availability/{id}` | Supprimer disponibilité | SALON_OWNER | 🟢 MOYENNE |
| ❓ | GET | `/api/availability/{id}` | Obtenir disponibilité | Public | 🟢 MOYENNE |
| ❓ | GET | `/api/availability` | Toutes disponibilités | Public | 🟡 HAUTE |
| ❓ | GET | `/api/availability/date/{date}` | Disponibilités par date | Public | 🔴 CRITIQUE |
| ❓ | GET | `/api/availability/available/{date}` | Créneaux libres | Public | 🔴 CRITIQUE |
| ❓ | GET | `/api/availability/period` | Disponibilités période | Public | 🟡 HAUTE |

---

## 💳 MODULE PAIEMENT (23 endpoints)

### PaymentController - `/api/payments` (9 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | POST | `/api/payments` | Créer paiement | Authentifié | 🔴 CRITIQUE |
| ❓ | POST | `/api/payments/confirm/{sessionId}` | Confirmer paiement | Public | 🔴 CRITIQUE |
| ❓ | GET | `/api/payments/{paymentId}` | Obtenir paiement | Public | 🟡 HAUTE |
| ❓ | GET | `/api/payments/customer/{customerId}` | Paiements client | Public | 🟢 MOYENNE |
| ❓ | GET | `/api/payments/salon` | Paiements salon | Public | 🟢 MOYENNE |
| ❓ | GET | `/api/payments/booking/{bookingId}` | Paiements réservation | Public | 🟡 HAUTE |
| ❓ | POST | `/api/payments/{paymentId}/refund` | Rembourser | Public | 🟢 MOYENNE |
| ❓ | POST | `/api/payments/{paymentId}/cancel` | Annuler paiement | Public | 🟢 MOYENNE |
| ❓ | GET | `/api/payments/salon/stats` | Stats paiements | Public | 🟢 MOYENNE |

### StripeCheckoutController - `/api/payments/checkout` (4 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | POST | `/api/payments/checkout/create-session` | Créer session Stripe | Authentifié | 🔴 CRITIQUE |
| ❓ | POST | `/api/payments/checkout/webhook` | Webhook Stripe | Public | 🔴 CRITIQUE |
| ❓ | GET | `/api/payments/checkout/session/{sessionId}` | Détails session | Public | 🟡 HAUTE |
| ❓ | POST | `/api/payments/checkout/session/{sessionId}/expire` | Expirer session | Public | 🔵 BASSE |

### StripeWebhookController - `/api/payments` (6 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | POST | `/api/payments/webhook` | Webhook principal | Public | 🔴 CRITIQUE |
| ❓ | GET | `/api/payments/webhook/migration-status` | Statut migration | Public | 🔵 BASSE |
| ❓ | GET | `/api/payments/webhook/stats` | Stats webhooks | Public | 🔵 BASSE |
| ❓ | POST | `/api/payments/webhook/release-lock/{eventId}` | Débloquer event | Public | 🔵 BASSE |
| ❓ | POST | `/api/payments/webhook/cleanup` | Nettoyer events | Public | 🔵 BASSE |
| ❓ | GET | `/api/payments/webhook/health` | Santé webhooks | Public | 🔵 BASSE |

### StripeTestController - `/api/payments/test` (4 endpoints - DEV ONLY)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | GET | `/api/payments/test/stripe-status` | Statut Stripe | Public | 🔵 TEST |
| ❓ | POST | `/api/payments/test/create-test-session` | Session test | Public | 🔵 TEST |
| ❓ | GET | `/api/payments/test/session/{sessionId}` | Récup session test | Public | 🔵 TEST |
| ❓ | GET | `/api/payments/test/publishable-key` | Clé publique | Public | 🟡 HAUTE |

---

## ❌ MODULE CATÉGORIE (DÉSACTIVÉ - NON UTILISÉ)

**Note**: Le système de catégories n'est plus utilisé à 100%. Les endpoints CategoryController et SalonCategoryController sont désactivés et ne seront pas connectés au frontend.

---

## 💇 MODULE SERVICE (8 endpoints)

### ServiceOfferingController - `/api/service-offerings` (6 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | GET | `/api/service-offerings` | Tous les services | Public | 🔴 CRITIQUE |
| ❓ | GET | `/api/service-offerings/{id}` | Service par ID | Public | 🔴 CRITIQUE |
| ❓ | GET | `/api/service-offerings/list/{idsString}` | Services multiples | Public | 🔴 CRITIQUE |
| ❓ | POST | `/api/service-offerings` | Créer service | Public | 🔴 CRITIQUE |
| ❓ | PUT | `/api/service-offerings/{id}` | Modifier service | Public | 🟡 HAUTE |
| ❓ | DELETE | `/api/service-offerings/{id}` | Supprimer service | Public | 🟢 MOYENNE |

### SalonServiceOfferingController - `/api/service-offering/salon-owner` (2 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | POST | `/api/service-offering/salon-owner` | Créer service | SALON_OWNER | 🟡 HAUTE |
| ❓ | PUT | `/api/service-offering/salon-owner/{id}` | Modifier service | SALON_OWNER | 🟢 MOYENNE |

---

## 🏪 MODULE SALON (7 endpoints)

### SettingsController - `/api/settings` (3 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | GET | `/api/settings` | Paramètres salon | Authentifié | 🔴 CRITIQUE |
| ❓ | PUT | `/api/settings` | Modifier paramètres | ADMIN | 🟡 HAUTE |
| ❓ | GET | `/api/settings/configured` | Salon configuré? | Public | 🔴 CRITIQUE |

### GoogleMapsPublicController - `/api/maps` (4 endpoints)

| ✓ | Méthode | Endpoint | Description | Autorisation | Priorité |
|---|---------|----------|-------------|--------------|----------|
| ❓ | GET | `/api/maps/config` | Config Google Maps | Public | 🟡 HAUTE |
| ❓ | GET | `/api/maps/salon-location` | Localisation salon | Public | 🔴 CRITIQUE |
| ❓ | POST | `/api/maps/salon/geocode` | Géocoder salon | Public | 🟢 MOYENNE |
| ❓ | GET | `/api/maps/status` | Statut Maps | Public | 🔵 BASSE |

---

## 🔔 MODULE WEBSOCKET (6 endpoints STOMP)

### NotificationWebSocketController - WebSocket STOMP

| ✓ | Type | Endpoint | Description | Autorisation | Priorité |
|---|------|----------|-------------|--------------|----------|
| ❓ | SUBSCRIBE | `/user/queue/notifications` | Notifications perso | Authentifié | 🔴 CRITIQUE |
| ❓ | SUBSCRIBE | `/topic/salon` | MAJ salon | Authentifié | 🟡 HAUTE |
| ❓ | MESSAGE | `/ping` | Ping connexion | Authentifié | 🟢 MOYENNE |
| ❓ | SUBSCRIBE | `/topic/availability` | MAJ disponibilités | Authentifié | 🟡 HAUTE |
| ❓ | MESSAGE | `/notification-preferences/{userId}` | MAJ préférences | Authentifié | 🟢 MOYENNE |
| ❓ | MESSAGE | `/test-connection` | Test WebSocket | Authentifié | 🔵 BASSE |

---

## 📈 STATISTIQUES PAR PRIORITÉ (SANS CATÉGORIES)

| Priorité | Nombre | Pourcentage | Description |
|----------|--------|-------------|-------------|
| 🔴 CRITIQUE | 26 | 31% | Fonctionnalités essentielles |
| 🟡 HAUTE | 24 | 29% | Fonctionnalités importantes |
| 🟢 MOYENNE | 19 | 23% | Fonctionnalités utiles |
| 🔵 BASSE | 10 | 12% | Fonctionnalités optionnelles |
| 🔵 TEST | 4 | 5% | Endpoints de test uniquement |

---

## 🎯 PLAN DE CONNEXION RECOMMANDÉ

### Phase 1 - Fondations (🔴 CRITIQUE)
1. **Authentification** : login, register, logout, me
2. **Services & Catégories** : Liste des services disponibles
3. **Disponibilités** : Vérification des créneaux libres
4. **Réservations** : Création et consultation
5. **Paiements** : Création session Stripe
6. **Paramètres** : Configuration salon

### Phase 2 - Fonctionnalités Principales (🟡 HAUTE)
1. **Gestion utilisateur** : Profil, mise à jour
2. **Gestion réservations** : Annulation, confirmation
3. **Gestion salon** : Pour les propriétaires
4. **Cartes** : Localisation du salon

### Phase 3 - Fonctionnalités Avancées (🟢 MOYENNE)
1. **WebSocket** : Notifications temps réel
2. **Statistiques** : Tableaux de bord
3. **Gestion complète** : CRUD complet

### Phase 4 - Finitions (🔵 BASSE)
1. **Admin** : Fonctionnalités admin
2. **Monitoring** : Santé, debug
3. **Tests** : Endpoints de test

---

## 🔗 PROCHAINES ÉTAPES

1. **Analyser le frontend** pour identifier les services/endpoints déjà connectés
2. **Créer/Mettre à jour** les services Angular manquants
3. **Connecter** les endpoints par ordre de priorité
4. **Tester** chaque connexion
5. **Documenter** les intégrations

---

**Note**: Ce document sera mis à jour au fur et à mesure de la connexion des endpoints.
La colonne "✓" sera cochée (✅) une fois l'endpoint connecté et testé.