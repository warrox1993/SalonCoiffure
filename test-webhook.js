/**
 * Script de test pour vérifier le webhook Stripe
 * Usage: node test-webhook.js
 */

const BASE_URL = 'http://localhost:7777';

async function login() {
    console.log('🔐 Connexion admin2...');
    
    try {
        const response = await fetch(`${BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                usernameOrEmail: 'admin2',
                password: 'Admin123!'
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            console.log('✅ Connexion réussie!');
            
            // Extraire le cookie de session pour les prochaines requêtes
            const cookies = response.headers.get('set-cookie');
            return cookies;
        } else {
            console.log(`❌ Erreur login: ${data.error || data.message}`);
            return null;
        }
        
    } catch (error) {
        console.log(`❌ Erreur réseau login: ${error.message}`);
        return null;
    }
}

async function createStripeSession(cookies) {
    console.log('🚀 Création de la session Stripe...');
    
    try {
        const response = await fetch(`${BASE_URL}/api/payments/checkout/create-session`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Cookie': cookies || ''
            },
            body: JSON.stringify({
                bookingId: 1,
                amount: 25.00,
                currency: 'EUR',
                paymentMethod: 'CARD',
                description: 'Test webhook métadonnées'
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.sessionId) {
            console.log('✅ Session créée avec succès!');
            console.log(`📋 Session ID: ${data.sessionId}`);
            console.log(`🔗 URL Stripe: ${data.url}`);
            
            return {
                sessionId: data.sessionId,
                url: data.url
            };
        } else {
            console.log(`❌ Erreur: ${JSON.stringify(data, null, 2)}`);
            return null;
        }
        
    } catch (error) {
        console.log(`❌ Erreur réseau: ${error.message}`);
        return null;
    }
}

async function testWebhook() {
    console.log('🧪 === TEST WEBHOOK STRIPE AFROSTYLE ===\n');
    
    // Étape 1: Login
    const cookies = await login();
    if (!cookies) {
        console.log('❌ Impossible de se connecter');
        return;
    }
    
    // Étape 2: Créer session Stripe
    const session = await createStripeSession(cookies);
    if (!session) {
        console.log('❌ Impossible de créer la session Stripe');
        return;
    }
    
    console.log('\n🎯 SESSION CRÉÉE AVEC SUCCÈS!');
    console.log('📝 Métadonnées attendues dans le webhook:');
    console.log('   - booking_id: "1"');
    console.log('   - customer_id: "152" (ID de admin2)');
    console.log('\n💡 PROCHAINES ÉTAPES:');
    console.log('1. Ouvrez cette URL dans votre navigateur:');
    console.log(`   ${session.url}`);
    console.log('2. Utilisez la carte de test: 4242 4242 4242 4242');
    console.log('3. Surveillez les logs de l\'application pour voir:');
    console.log('   📋 Booking ID extracted: 1');
    console.log('   📋 Customer ID extracted: 152');
    console.log('   ✅ Payment status updated to SUCCEEDED');
}

// Exécuter le test
testWebhook().catch(console.error);