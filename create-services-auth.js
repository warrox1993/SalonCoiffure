const services = [
  {
    name: "Box Braids",
    description: "Tresses protectrices élégantes avec extensions, idéales pour protéger vos cheveux naturels",
    price: 65.00,
    duration: 240,
    images: "assets/2.jpg"
  },
  {
    name: "Entretien Locks",
    description: "Entretien professionnel de vos dreadlocks avec retouches des racines et soins",
    price: 40.00,
    duration: 90,
    images: "assets/3.jpg"
  },
  {
    name: "Soins Capillaires",
    description: "Traitement hydratant en profondeur pour cheveux abîmés et secs",
    price: 35.00,
    duration: 60,
    images: "assets/4.jpg"
  },
  {
    name: "Défrisage Naturel",
    description: "Lissage permanent sans produits chimiques agressifs, respectueux de vos cheveux",
    price: 85.00,
    duration: 180,
    images: "assets/5.jpg"
  },
  {
    name: "Tissage Brésilien",
    description: "Pose de tissage avec cheveux naturels brésiliens de haute qualité",
    price: 120.00,
    duration: 180,
    images: "assets/6.jpg"
  },
  {
    name: "Pose de Dreadlocks",
    description: "Création de dreadlocks naturelles avec technique professionnelle",
    price: 95.00,
    duration: 300,
    images: "assets/7.jpg"
  },
  {
    name: "Coloration Premium",
    description: "Coloration professionnelle avec produits haut de gamme sans ammoniaque",
    price: 55.00,
    duration: 120,
    images: "assets/8.jpg"
  },
  {
    name: "Soin Profond Kératine",
    description: "Traitement à la kératine pour réparer et lisser les cheveux en profondeur",
    price: 75.00,
    duration: 90,
    images: "assets/1.jpg"
  },
  {
    name: "Coupe Homme Moderne",
    description: "Coupe tendance avec dégradé et finitions au rasoir pour homme",
    price: 20.00,
    duration: 30,
    images: "assets/2.jpg"
  },
  {
    name: "Coiffure Enfant",
    description: "Coupe et coiffure adaptées aux enfants avec patience et douceur",
    price: 15.00,
    duration: 30,
    images: "assets/3.jpg"
  }
];

// D'abord se connecter pour obtenir le token
async function login() {
  const response = await fetch('http://localhost:7777/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      usernameOrEmail: 'admin2',
      password: 'Admin123!'
    })
  });
  
  if (response.ok) {
    const data = await response.json();
    console.log('✅ Connexion réussie');
    return data.token;
  } else {
    throw new Error('Échec de connexion');
  }
}

// Fonction pour créer les services avec authentification
async function createServices() {
  try {
    const token = await login();
    
    for (const service of services) {
      try {
        const response = await fetch('http://localhost:7777/api/service-offering/salon-owner', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(service)
        });
        
        if (response.ok) {
          const result = await response.json();
          console.log(`✅ Service créé : ${service.name} (ID: ${result.id})`);
        } else {
          const error = await response.text();
          console.error(`❌ Erreur création ${service.name}:`, response.status, error);
        }
      } catch (error) {
        console.error(`❌ Erreur création ${service.name}:`, error);
      }
    }
  } catch (error) {
    console.error('❌ Erreur de connexion:', error);
  }
}

createServices();