// Translation texts
//import getData from "./api.js";

const texts = {
  en: {
    loginTitle: "Login",
    labelEmail: "Email:",
    labelPassword: "Password:",
    labelRole: "Role:",
    loginButton: "Login",
    placeholderEmail: "Enter your email",
    placeholderPassword: "Enter your password",
    optionUser: "Doctor",
    optionAdmin: "Admin",
    panelTitle1: "Doctor Panel",
    panelTitle2: "Admin Panel",
    recordTitle: "Add Medical Record",
    labelAnimalId: "Animal ID:",
    labelDate: "Date:",
    labelDescription: "Description:",
    labelUserId: "User ID:",
    submitButton: "Submit",
    backButton: "← Back",
    toggleButton: "Create Record"
  },
  uk: {
    loginTitle: "Вхід",
    labelEmail: "Електронна пошта:",
    labelPassword: "Пароль:",
    labelRole: "Роль:",
    loginButton: "Увійти",
    placeholderEmail: "Введіть електронну пошту",
    placeholderPassword: "Введіть пароль",
    optionUser: "Лікар",
    optionAdmin: "Адміністратор",
    panelTitle1: "Панель лікаря",
    panelTitle2: "Панель адміністратора",
    recordTitle: "Додати медичний запис",
    labelAnimalId: "ID тварини:",
    labelDate: "Дата:",
    labelDescription: "Опис:",
    labelUserId: "ID користувача:",
    submitButton: "Зберегти",
    backButton: "← Назад",
    toggleButton: "Створити запис"
  }
};

let currentLang = 'en';
let userRole = '';
let CurrentType = 'medical';

let records = []; // Данные с бекенда сюда будут загружаться
let nextId = 1;  // Можно потом синхронизировать с бекендом, если нужно
let editingRecordId = null;

// Switch UI language
function switchLanguage(lang) {
  currentLang = lang;
  const t = texts[lang];
  document.getElementById('login-title').innerText = t.loginTitle;
  document.getElementById('label-email').innerText = t.labelEmail;
  document.getElementById('label-password').innerText = t.labelPassword;
  document.getElementById('label-role').innerText = t.labelRole;
  document.getElementById('login-button').innerText = t.loginButton;
  document.getElementById('email').placeholder = t.placeholderEmail;
  document.getElementById('password').placeholder = t.placeholderPassword;
  document.getElementById('option-user').innerText = t.optionUser;
  document.getElementById('option-admin').innerText = t.optionAdmin;
  document.getElementById('panel-title1').innerText = t.panelTitle1;
  document.getElementById('panel-title2').innerText = t.panelTitle2;
  document.getElementById('record-title').innerText = t.recordTitle;
  document.getElementById('label-animal-id').innerText = t.labelAnimalId;
  document.getElementById('label-date').innerText = t.labelDate;
  document.getElementById('label-description').innerText = t.labelDescription;
  document.getElementById('label-user-id').innerText = t.labelUserId;
  document.getElementById('submit-button').innerText = t.submitButton;
  document.getElementById('back-button-top').innerText = t.backButton;
  document.querySelectorAll('.toggle-button').forEach(btn => btn.innerText = t.toggleButton);
  renderRecords(CurrentType);
}

async function login() {
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value.trim();
  const selectedRole = document.getElementById('role').value; // 'doctor' или 'admin'

  if (!email || !password) {
    alert(currentLang === 'uk' ? 'Будь ласка, заповніть усі поля.' : 'Please fill in all fields.');
    return;
  }

  // Получаем пользователей с сервера (или из локальных данных)
  const users = await fetchUsers();

  // Находим пользователя по email
  const user = users.find(u => u.email.toLowerCase() === email.toLowerCase());

  if (!user) {
    alert(currentLang === 'uk' ? 'Користувача не знайдено.' : 'User not found.');
    return;
  }

  // Проверяем пароль (если на бэкенде нет хеширования, просто сравним)
  if (user.password !== password) {
    alert(currentLang === 'uk' ? 'Невірний пароль.' : 'Incorrect password.');
    return;
  }

  // Проверяем роль: врач = 0, админ = 2
  // В форме роль выбрана строкой 'doctor' или 'admin', сопоставим с числами
  const roleMap = { doctor: 0, admin: 2 };
  const expectedRole = roleMap[selectedRole];

  if (user.role !== expectedRole) {
    alert(currentLang === 'uk' ? 'У вас немає доступу до цієї ролі.' : 'You do not have access to this role.');
    return;
  }

  userRole = selectedRole;

  // Если админ - переход на админскую страницу
  if (selectedRole === 'admin') {
    window.location.href = 'admin.html';
    return;
  }

  // Если врач - показать врачебную панель
  document.getElementById('login').style.display = 'none';
  document.getElementById('back-button-top').style.display = 'inline-block';
  document.getElementById('doctor-dashboard').style.display = 'block';

  updateRoleVisibility();
  renderRecords(CurrentType);
}


// Handle logout/back
function goBack() {
  document.getElementById('login').style.display = 'block';
  document.getElementById('doctor-dashboard').style.display = 'none';
  document.getElementById('panel').style.display = 'none';
  document.getElementById('record-panel').style.display = 'none';
  document.getElementById('modal-overlay').style.display = 'none';
  document.getElementById('back-button-top').style.display = 'none';
  editingRecordId = null;
}

// Open modal for new record
function openNewRecord() {
  editingRecordId = null;
  document.getElementById('record-id').value = '';
  document.getElementById('animal-id').value = '';
  document.getElementById('date').value = '';
  document.getElementById('description').value = '';
  document.getElementById('user-id').value = '';
  document.getElementById('record-panel').style.display = 'block';
  document.getElementById('modal-overlay').style.display = 'block';
}

// Close modal
function closePanel() {
  document.getElementById('record-panel').style.display = 'none';
  document.getElementById('modal-overlay').style.display = 'none';
  editingRecordId = null;
}

async function submitRecord() {
  try {
    const recordId = document.getElementById('record-id')?.value || null;
    const animalId = parseInt(document.getElementById('animal-id').value);
    const userId = parseInt(document.getElementById('user-id').value);
    const date = document.getElementById('date').value;
    const description = document.getElementById('description').value;

    if (!animalId || !userId || !date || !description) {
      alert('Please fill all fields!');
      return;
    }

    // Отримуємо об'єкти тварини та користувача (заповнюємо shelter)
    const animal = await getAnimalById(animalId);
    const user = await getUserById(userId);

    if (!animal || !user) {
      alert('Invalid Animal or User ID');
      return;
    }

    // Додатково перевіряємо shelter у animal
    if (!animal.shelter) {
      // Можеш або отримати shelter через API, або підставити мінімальний об'єкт:
      animal.shelter = {
        id: 1, 
        name: "Default Shelter", 
        location: "Unknown", 
        contacts: "N/A"
      };
    }

    const payload = {
      id: recordId ? parseInt(recordId) : 0,
      animalId: animalId,
      animal: animal,
      userId: userId,
      user: user,
      date: date,
      description: description
    };

    const url = recordId 
      ? `http://localhost:5104/api/MedicalRecords/${recordId}`
      : `http://localhost:5104/api/MedicalRecords`;

    const method = recordId ? 'PUT' : 'POST';

    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const errorData = await response.json();
      console.error('Error updating record:', errorData);
      alert('Failed to submit record: ' + JSON.stringify(errorData));
      return;
    }

    alert('Record saved successfully!');
    closePanel();
    await renderRecords(CurrentType);
  } catch (error) {
    console.error('submitRecord error:', error);
    alert('Unexpected error occurred');
  }
}

// Заглушка для getAnimalById з Shelter
async function getAnimalById(id) {
  return { 
    id: id, 
    name: "AnimalName", 
    species: "Species", 
    breed: "Breed", 
    age: 5, 
    shelter: {
      id: 1,
      name: "Shelter Name",
      location: "Shelter Location",
      contacts: "Shelter Contacts"
    },
    shelterId: 1
  };
}

async function getUserById(id) {
  return { id: id, name: "UserName", role: 1, email: "user@example.com", password: "" };
}


// Приклад заглушок для отримання об'єктів Animal і User
async function getAnimalById(id) {
  // Тут виконай fetch з API або візьми зі стану додатку
  // Поки що просто повертаємо мінімальний об'єкт:
  return { id: id, name: "AnimalName", species: "Species", breed: "Breed", age: 5, shelterId: 1 };
}

async function getUserById(id) {
  // Аналогічно
  return { id: id, name: "UserName", role: 1, email: "user@example.com", password: "" };
}


// Render records in both doctor and admin lists
async function renderRecords(type) {
  CurrentType = type;

  // Загрузка данных в зависимости от типа
  switch(type) {
    case 'animals':
      records = await fetchAnimals();
      break;
    case 'users':
      records = await fetchUsers();
      break;
    case 'medical':
      records = await fetchMedicalRecords();
      break;
    case 'reports':
      records = await fetchReports();
      break;
    case 'sensors':
      records = await fetchSensors();
      break;
    default:
      records = [];
  }

  const doctorList = document.getElementById('doctor-record-list');
  const adminList = document.getElementById('admin-record-list');

  if (doctorList) {
    doctorList.innerHTML = '';
    records.forEach(rec => {
      const docEntry = document.createElement('div');
      docEntry.className = 'record-entry';
      const docText = document.createElement('span');
      docText.className = 'record-text';
      docText.innerText = `🐾 ID: ${rec.animalId}, Date: ${rec.date}, User: ${rec.userId}, Desc: ${rec.description}`;
      docText.onclick = () => {
        if (userRole === 'doctor') editRecord(rec.id);
      };
      docEntry.appendChild(docText);
      doctorList.appendChild(docEntry);
    });
  }

  if (adminList) {
    adminList.innerHTML = '';
    records.forEach(rec => {
      const adminEntry = document.createElement('div');
      adminEntry.className = 'record-entry';
      const adminText = document.createElement('span');
      adminText.className = 'record-text';
      adminText.innerText = GetInnerText(type, rec);
      adminText.onclick = () => editRecord(rec.id);
      const deleteBtn = document.createElement('button');
      deleteBtn.className = 'delete-button';
      deleteBtn.innerText = currentLang === 'uk' ? 'Видалити' : 'Delete';
      deleteBtn.onclick = () => deleteRecord(rec.id);
      adminEntry.appendChild(adminText);
      adminEntry.appendChild(deleteBtn);
      adminList.appendChild(adminEntry);
    });
  }
}

// Edit record by opening modal with fields populated
function editRecord(id) {
  const rec = records.find(r => r.id === id);
  if (!rec) return;
  editingRecordId = id;
  document.getElementById('record-id').value = id;
  document.getElementById('animal-id').value = rec.animalId || rec.Id || '';
  document.getElementById('date').value = rec.date || rec.InstallationDate || '';
  document.getElementById('description').value = rec.description || '';
  document.getElementById('user-id').value = rec.userId || rec.UserId || '';
  document.getElementById('record-panel').style.display = 'block';
  document.getElementById('modal-overlay').style.display = 'block';
}

// Delete record (запрос на удаление)
async function deleteRecord(id) {
  if (!confirm(currentLang === 'uk' ? 'Видалити запис?' : 'Delete this record?')) return;
  try {
    const url = getApiUrlForDelete(id);
    const response = await fetch(url, { method: 'DELETE' });
    if (response.ok) {
      alert(currentLang === 'uk' ? 'Запис видалено!' : 'Record deleted!');
      await renderRecords(CurrentType);
    } else {
      throw new Error('Delete failed');
    }
  } catch (err) {
    alert(currentLang === 'uk' ? 'Помилка видалення.' : 'Delete error.');
  }
}

// Форматирование записи для отображения
function GetInnerText(type, rec) {
  switch(type) {
    case 'animals':
      return `🐾 ID: ${rec.id}, Species: ${rec.species}, Name: ${rec.name}, Age: ${rec.age}`;
    case 'users':
      return `👤 ID: ${rec.id}, Email: ${rec.email}, Role: ${rec.role}`;
    case 'medical':
      return `📅 Date: ${rec.date}, Animal ID: ${rec.animalId}, User ID: ${rec.userId}, Desc: ${rec.description}`;
    case 'reports':
      return `📝 Report ID: ${rec.id}, Animal ID: ${rec.animalId}, Date: ${rec.date}, Text: ${rec.reportText}`;
    case 'sensors':
      return `🔧 Sensor ID: ${rec.id}, Animal ID: ${rec.animalId}, Type: ${rec.type}, Value: ${rec.value}`;
    default:
      return JSON.stringify(rec);
  }
}

// API URLs - замените на свои реальные URL
const API_BASE = 'http://localhost:5104/api';

async function fetchAnimals() {
  try {
    const res = await fetch(`${API_BASE}/Animal`);
    if (!res.ok) throw new Error('Failed to fetch animals');
    return await res.json();
  } catch (err) {
    alert(currentLang === 'uk' ? 'Помилка завантаження тварин.' : 'Failed to load animals.');
    return [];
  }
}

async function fetchUsers() {
  try {
    const res = await fetch(`${API_BASE}/Users`);
    if (!res.ok) throw new Error('Failed to fetch users');
    return await res.json();
  } catch {
    alert(currentLang === 'uk' ? 'Помилка завантаження користувачів.' : 'Failed to load users.');
    return [];
  }
}

async function fetchMedicalRecords() {
  try {
    const res = await fetch(`${API_BASE}/MedicalRecords`);
    if (!res.ok) throw new Error('Failed to fetch medical records');
    return await res.json();
  } catch {
    alert(currentLang === 'uk' ? 'Помилка завантаження медичних записів.' : 'Failed to load medical records.');
    return [];
  }
}

async function fetchReports() {
  try {
    const res = await fetch(`${API_BASE}/Reports`);
    if (!res.ok) throw new Error('Failed to fetch reports');
    return await res.json();
  } catch {
    alert(currentLang === 'uk' ? 'Помилка завантаження звітів.' : 'Failed to load reports.');
    return [];
  }
}

async function fetchSensors() {
  try {
    const res = await fetch(`${API_BASE}/Sensors`);
    if (!res.ok) throw new Error('Failed to fetch sensors');
    return await res.json();
  } catch {
    alert(currentLang === 'uk' ? 'Помилка завантаження сенсорів.' : 'Failed to load sensors.');
    return [];
  }
}

// Return API URL for DELETE requests based on current type and record id
function getApiUrlForDelete(id) {
  switch(CurrentType) {
    case 'animals': return `${API_BASE}/Animal/${id}`;
    case 'users': return `${API_BASE}/Users/${id}`;
    case 'medical': return `${API_BASE}/MedicalRecords/${id}`;
    case 'reports': return `${API_BASE}/Reports/${id}`;
    case 'sensors': return `${API_BASE}/Sensors/${id}`;
    default: return `${API_BASE}/MedicalRecords/${id}`;
  }
}


function updateRoleVisibility() {
  // Показывать/скрывать кнопки и панели в зависимости от роли
  if (userRole === 'doctor') {
    document.getElementById('doctor-panel').style.display = 'block';
    document.getElementById('admin-panel').style.display = 'none';
  } else if (userRole === 'admin') {
    document.getElementById('doctor-panel').style.display = 'none';
    document.getElementById('admin-panel').style.display = 'block';
  }
}

window.onload = () => {
  switchLanguage(currentLang);
};
