// Translation texts
const adminTexts = {
  en: {
    adminHeader:"Admin Panel",
    animals: "Animals",
    staff: "Staff",
    sensors: "Sensors",
    medicalReports: "Medical Reports",
    generalReports: "General Reports",
    add: "Add",
    edit: "Edit",
    delete: "Delete",
    logout: "Logout",
    newButton: "Create record",
    backButton: "Back",
  },
  uk: {
    adminHeader:"Панель Адміністрування",
    animals: "Тварини",
    staff: "Працівники",
    sensors: "Сенсори",
    medicalReports: "Медичні звіти",
    generalReports: "Звіти",
    add: "Додати",
    edit: "Редагувати",
    delete: "Видалити",
    logout: "Вийти",
    newButton: "Новий запис",
    backButton: "Назад",
  }
};

let adminLang = 'en';
let adminData = {
  animals: [],
  staff: [],
  sensors: [],
  medicalReports: [],
  generalReports: []
};

function switchAdminLanguage(lang) {
  currentLang = lang;
      const t = adminTexts[lang];
      document.getElementById('admin-title').innerText = t.adminHeader;
      document.getElementById('animals-button').innerText = t.animals;
      document.getElementById('employees-button').innerText = t.staff;
      document.getElementById('sensors-button').innerText = t.sensors;
      document.getElementById('medical-button').innerText = t.medicalReports;
      document.getElementById('reports-button').innerText = t.generalReports;
      document.getElementById('logout-button').innerText = t.logout;
      document.getElementById('new-button').innerText = t.newButton;
      document.getElementById('back-button').innerText = t.backButton;
      renderAdminList();
}

function showAdminSection(section) {
  document.querySelectorAll('.admin-section').forEach(el => {
    el.style.display = 'none';
  });
  document.getElementById(`admin-${section}`).style.display = 'block';
  renderAdminList(section);
}

function renderAdminList(section) {
  const container = document.getElementById(`list-${section}`);
  container.innerHTML = '';
  adminData[section].forEach((item, index) => {
    const entry = document.createElement('div');
    entry.className = 'record-entry';
    entry.innerHTML = `
      <span class="record-text">${JSON.stringify(item)}</span>
      <button data-action="edit" onclick="editAdminRecord('${section}', ${index})">${adminTexts[adminLang].edit}</button>
      <button data-action="delete" onclick="deleteAdminRecord('${section}', ${index})">${adminTexts[adminLang].delete}</button>
    `;
    container.appendChild(entry);
  });
}

function addAdminRecord(section) {
  const newItem = prompt(adminLang === 'uk' ? 'Введіть нові дані (у JSON-форматі)' : 'Enter new data (in JSON format)');
  try {
    const parsed = JSON.parse(newItem);
    adminData[section].push(parsed);
    renderAdminList(section);
  } catch (e) {
    alert(adminLang === 'uk' ? 'Неправильний формат JSON' : 'Invalid JSON format');
  }
}

function editAdminRecord(section, index) {
  const current = adminData[section][index];
  const updated = prompt(adminLang === 'uk' ? 'Редагувати дані (у JSON-форматі)' : 'Edit data (in JSON format)', JSON.stringify(current));
  try {
    const parsed = JSON.parse(updated);
    adminData[section][index] = parsed;
    renderAdminList(section);
  } catch (e) {
    alert(adminLang === 'uk' ? 'Неправильний формат JSON' : 'Invalid JSON format');
  }
}

function deleteAdminRecord(section, index) {
  if (confirm(adminLang === 'uk' ? 'Ви впевнені, що хочете видалити?' : 'Are you sure you want to delete?')) {
    adminData[section].splice(index, 1);
    renderAdminList(section);
  }
}

function showSection(section){
  document.getElementById("admin-nav").style.display = "none";
  document.getElementById("section-container").style.display = "block";
  renderRecords(section);
}

function backButton(){
  document.getElementById("admin-nav").style.display = "block";
  document.getElementById("section-container").style.display = "none";
}

function logout(section, index) {
  history.back();
}

