// Global State
const state = {
    currentUser: null,
    currentSection: 'home',
    goals: [],
    roadmaps: [],
    topics: [],
    currentRoadmap: null,
    currentAssessmentQuestion: null,
    sessionId: null,
    messages: []
};

const API_BASE = 'http://localhost:8082';

// ===== INITIALIZATION =====
window.addEventListener('load', () => {
    initializeApp();
    loadDashboard();
});

async function initializeApp() {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    
    if (!token || !userId) {
        redirectToLogin();
        return;
    }
    
    state.currentUser = { id: userId, token: token };
    await loadGoals();
    await loadTopics();
}

function redirectToLogin() {
    window.location.href = '/login.html';
}

// ===== SECTION NAVIGATION =====
function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(s => s.classList.add('hidden'));
    
    // Show selected section
    const section = document.getElementById(sectionName);
    if (section) {
        section.classList.remove('hidden');
        state.currentSection = sectionName;
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    redirectToLogin();
}

// ===== DASHBOARD =====
async function loadDashboard() {
    const userId = state.currentUser.id;
    
    // Load stats for home section
    await Promise.all([
        loadGoals(),
        loadUserSkillCoverage(userId)
    ]);
    
    const goalsCount = state.goals.length;
    document.getElementById('totalGoals').textContent = goalsCount;
}

// ===== GOALS MANAGEMENT =====
async function loadGoals() {
    try {
        const response = await fetch(`${API_BASE}/goals/user/${state.currentUser.id}`, {
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        if (!response.ok) throw new Error('Failed to load goals');
        state.goals = await response.json();
        
        renderGoals();
        populateGoalSelects();
    } catch (error) {
        console.error('Error loading goals:', error);
    }
}

function renderGoals() {
    const goalsList = document.getElementById('goalsList');
    goalsList.innerHTML = '';
    
    if (state.goals.length === 0) {
        goalsList.innerHTML = '<p class="message">No goals yet. Create one to get started!</p>';
        return;
    }
    
    state.goals.forEach(goal => {
        const card = document.createElement('div');
        card.className = 'goal-card';
        card.innerHTML = `
            <h3>${goal.name}</h3>
            <p>${goal.description || 'No description'}</p>
            <div class="goal-actions">
                <button onclick="selectGoal('${goal.id}')" class="btn-primary">View</button>
                <button onclick="generateRoadmapForGoal('${goal.id}')" class="btn-secondary">Generate Roadmap</button>
                <button onclick="deleteGoal('${goal.id}')" class="btn-secondary">Delete</button>
            </div>
        `;
        goalsList.appendChild(card);
    });
}

function populateGoalSelects() {
    const roadmapSelect = document.getElementById('goalSelect');
    const assessmentSelect = document.getElementById('topicSelectAssess');
    
    roadmapSelect.innerHTML = '<option value="">-- Select a Goal --</option>';
    
    state.goals.forEach(goal => {
        const option = document.createElement('option');
        option.value = goal.id;
        option.textContent = goal.name;
        roadmapSelect.appendChild(option);
    });
}

function showGoalForm() {
    document.getElementById('goalForm').classList.toggle('hidden');
}

function toggleGoalForm() {
    showGoalForm();
}

async function createGoal(event) {
    event.preventDefault();
    
    const name = document.getElementById('goalName').value;
    const description = document.getElementById('goalDescription').value;
    
    try {
        const response = await fetch(`${API_BASE}/goals/create?userId=${state.currentUser.id}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.currentUser.token}`
            },
            body: JSON.stringify({ name, description })
        });
        
        if (!response.ok) throw new Error('Failed to create goal');
        
        await loadGoals();
        document.getElementById('goalForm').reset();
        showGoalForm();
        alert('Goal created successfully!');
    } catch (error) {
        console.error('Error creating goal:', error);
        alert('Error creating goal');
    }
}

async function deleteGoal(goalId) {
    if (!confirm('Are you sure you want to delete this goal?')) return;
    
    try {
        await fetch(`${API_BASE}/goals/${goalId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        await loadGoals();
        alert('Goal deleted');
    } catch (error) {
        console.error('Error deleting goal:', error);
    }
}

function selectGoal(goalId) {
    const goal = state.goals.find(g => g.id === goalId);
    showSection('roadmap');
    document.getElementById('goalSelect').value = goalId;
    loadRoadmap();
}

// ===== ROADMAP MANAGEMENT =====
async function loadRoadmap() {
    const goalId = document.getElementById('goalSelect').value;
    if (!goalId) return;
    
    try {
        const goal = state.goals.find(g => g.id === goalId);
        document.getElementById('roadmapTitle').textContent = `${goal.name} Roadmap`;
        
        // Generate roadmap if it doesn't exist
        const response = await fetch(`${API_BASE}/roadmaps/generate?goalId=${goalId}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        if (!response.ok) throw new Error('Failed to generate roadmap');
        const roadmap = await response.json();
        state.currentRoadmap = roadmap;
        
        renderRoadmap(roadmap);
    } catch (error) {
        console.error('Error loading roadmap:', error);
    }
}

function renderRoadmap(roadmap) {
    const topicsList = document.getElementById('roadmapTopics');
    topicsList.innerHTML = '';
    
    if (!roadmap.roadmapTopics || roadmap.roadmapTopics.length === 0) {
        topicsList.innerHTML = '<p class="message">No topics in this roadmap yet.</p>';
        return;
    }
    
    roadmap.roadmapTopics.forEach((rt, index) => {
        const topic = rt.topic;
        const card = document.createElement('div');
        card.className = 'topic-card';
        
        const difficultyClass = topic.difficulty?.toLowerCase() || 'medium';
        const hours = topic.estimatedHours || 10;
        
        card.innerHTML = `
            <div class="topic-info">
                <h4>${index + 1}. ${topic.title}</h4>
                <p>${topic.description || 'No description'}</p>
            </div>
            <div class="topic-meta">
                <span class="difficulty-badge ${difficultyClass}">${topic.difficulty || 'MEDIUM'}</span>
                <span>${hours}h</span>
                <button onclick="assessTopic('${topic.id}', '${topic.title}')" class="btn-primary">Assess</button>
            </div>
        `;
        
        topicsList.appendChild(card);
    });
    
    document.getElementById('roadmapView').classList.remove('hidden');
    document.getElementById('roadmapContainer').classList.add('hidden');
}

async function generateNewRoadmap() {
    if (!state.currentRoadmap) return;
    
    const goalId = state.currentRoadmap.goal.id;
    
    try {
        const response = await fetch(`${API_BASE}/roadmaps/generate?goalId=${goalId}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        if (!response.ok) throw new Error('Failed to regenerate');
        const newRoadmap = await response.json();
        renderRoadmap(newRoadmap);
        alert('Roadmap regenerated!');
    } catch (error) {
        console.error('Error regenerating roadmap:', error);
        alert('Error regenerating roadmap');
    }
}

// ===== PROGRESS TRACKING =====
async function loadUserSkillCoverage(userId) {
    try {
        const response = await fetch(`${API_BASE}/progress/user/${userId}/coverage`, {
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        if (!response.ok) throw new Error('Failed to load coverage');
        const coverage = await response.json();
        
        // Update home stats
        document.getElementById('overallCoverage').textContent = Math.round(coverage.overallSkillPercentage) + '%';
        document.getElementById('masteredTopics').textContent = coverage.masteredTopics;
        
        // Update progress tab
        document.getElementById('overallBar').style.width = coverage.overallSkillPercentage + '%';
        document.getElementById('overallPercent').textContent = Math.round(coverage.overallSkillPercentage) + '%';
        document.getElementById('masteredCount').textContent = coverage.masteredTopics;
        document.getElementById('proficientCount').textContent = coverage.proficientTopics;
        document.getElementById('beginnerCount').textContent = coverage.beginnerTopics;
    } catch (error) {
        console.error('Error loading skill coverage:', error);
    }
}

function switchProgressTab(tab) {
    // Hide all tabs
    document.querySelectorAll('.progress-tab').forEach(t => t.classList.add('hidden'));
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    
    // Show selected tab
    document.getElementById(tab + 'Tab').classList.remove('hidden');
    event.target.classList.add('active');
    
    if (tab === 'topics') loadUserTopicsProgress();
    if (tab === 'roadmaps') loadRoadmapsProgress();
}

async function loadUserTopicsProgress() {
    try {
        const response = await fetch(`${API_BASE}/progress/user/${state.currentUser.id}`, {
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        if (!response.ok) throw new Error('Failed to load topics');
        const progresses = await response.json();
        
        const list = document.getElementById('userTopicsList');
        list.innerHTML = '';
        
        if (progresses.length === 0) {
            list.innerHTML = '<p class="message">No topics tracked yet.</p>';
            return;
        }
        
        progresses.forEach(p => {
            const item = document.createElement('div');
            item.className = 'topic-progress-item';
            
            const proficiency = Math.round(p.proficiency);
            const lastAssessed = p.lastAssessedAt ? new Date(p.lastAssessedAt).toLocaleDateString() : 'Never';
            
            item.innerHTML = `
                <h4>${p.topic.title}</h4>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${proficiency}%"></div>
                </div>
                <div class="progress-meta">
                    <span>${proficiency}% proficiency</span>
                    <span>Last assessed: ${lastAssessed}</span>
                </div>
            `;
            
            list.appendChild(item);
        });
    } catch (error) {
        console.error('Error loading topics progress:', error);
    }
}

async function loadRoadmapsProgress() {
    try {
        const select = document.getElementById('roadmapSelectProgress');
        select.innerHTML = '<option value="">-- Select a Roadmap --</option>';
        
        // Populate with available roadmaps (would need to load from backend)
        state.goals.forEach(goal => {
            const option = document.createElement('option');
            option.value = goal.id;
            option.textContent = goal.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading roadmaps:', error);
    }
}

async function loadRoadmapProgress() {
    const goalId = document.getElementById('roadmapSelectProgress').value;
    if (!goalId) return;
    
    try {
        // Need roadmap ID - this would require storing roadmap mapping
        const progressView = document.getElementById('roadmapProgressView');
        progressView.innerHTML = '<p class="message">Loading roadmap progress...</p>';
    } catch (error) {
        console.error('Error loading roadmap progress:', error);
    }
}

async function updateTopicProgress(topicId, proficiency, notes) {
    try {
        const response = await fetch(`${API_BASE}/progress/update-topic?userId=${state.currentUser.id}&topicId=${topicId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.currentUser.token}`
            },
            body: JSON.stringify({ proficiency, notes })
        });
        
        if (!response.ok) throw new Error('Failed to update progress');
        await loadUserSkillCoverage(state.currentUser.id);
    } catch (error) {
        console.error('Error updating progress:', error);
    }
}

// ===== TOPICS =====
async function loadTopics() {
    try {
        const response = await fetch(`${API_BASE}/topics/all`, {
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        if (!response.ok) throw new Error('Failed to load topics');
        state.topics = await response.json();
        
        populateTopicSelects();
    } catch (error) {
        console.error('Error loading topics:', error);
    }
}

function populateTopicSelects() {
    const assessSelect = document.getElementById('topicSelectAssess');
    assessSelect.innerHTML = '<option value="">-- Select a Topic --</option>';
    
    state.topics.forEach(topic => {
        if (!topic.parentTopic) { // Only show root topics
            const option = document.createElement('option');
            option.value = topic.id;
            option.textContent = topic.title;
            assessSelect.appendChild(option);
        }
    });
}

// ===== ASSESSMENT =====
async function generateQuestion() {
    const topicId = document.getElementById('topicSelectAssess').value;
    if (!topicId) {
        alert('Please select a topic');
        return;
    }
    
    const topic = state.topics.find(t => t.id === topicId);
    
    try {
        const response = await fetch(`${API_BASE}/assessments/generate-question`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.currentUser.token}`
            },
            body: JSON.stringify({
                topic: topic.title,
                userContext: `User assessing topic: ${topic.description}`
            })
        });
        
        if (!response.ok) throw new Error('Failed to generate question');
        state.currentAssessmentQuestion = await response.json();
        
        displayQuestion();
    } catch (error) {
        console.error('Error generating question:', error);
        alert('Error generating question');
    }
}

function displayQuestion() {
    if (!state.currentAssessmentQuestion) return;
    
    const q = state.currentAssessmentQuestion;
    document.getElementById('questionText').textContent = q.question;
    document.getElementById('userAnswerInput').value = '';
    
    document.getElementById('assessmentView').classList.remove('hidden');
    document.getElementById('feedbackView').classList.add('hidden');
}

async function submitAnswer() {
    if (!state.currentAssessmentQuestion) return;
    
    const userAnswer = document.getElementById('userAnswerInput').value;
    if (!userAnswer.trim()) {
        alert('Please provide an answer');
        return;
    }
    
    try {
        const expectedKeywords = JSON.stringify(state.currentAssessmentQuestion.expectedKeywords);
        
        const response = await fetch(`${API_BASE}/assessments/evaluate-answer`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.currentUser.token}`
            },
            body: JSON.stringify({
                question: state.currentAssessmentQuestion.question,
                expectedKeywords: expectedKeywords,
                userAnswer: userAnswer
            })
        });
        
        if (!response.ok) throw new Error('Failed to evaluate answer');
        const result = await response.json();
        
        displayFeedback(result);
    } catch (error) {
        console.error('Error evaluating answer:', error);
    }
}

function displayFeedback(result) {
    const feedbackContent = document.getElementById('feedbackContent');
    const correctionStatus = result.correct ? '✓ Correct' : '✗ Needs Improvement';
    
    feedbackContent.innerHTML = `
        <h4>${correctionStatus}</h4>
        <p><strong>Score:</strong> ${result.score}%</p>
        <p><strong>Feedback:</strong> ${result.feedback}</p>
        <p><strong>Explanation:</strong> ${state.currentAssessmentQuestion.explanation}</p>
    `;
    
    document.getElementById('feedbackView').classList.remove('hidden');
}

function assessTopic(topicId, topicTitle) {
    showSection('assessment');
    const select = document.getElementById('topicSelectAssess');
    select.value = topicId;
    generateQuestion();
}

// ===== PROJECTS =====
function showProjectForm() {
    document.getElementById('projectForm').classList.toggle('hidden');
}

function toggleProjectForm() {
    showProjectForm();
}

async function submitProject(event) {
    event.preventDefault();
    
    const title = document.getElementById('projectTitle').value;
    const description = document.getElementById('projectDescription').value;
    const link = document.getElementById('projectLink').value;
    
    try {
        const response = await fetch(`${API_BASE}/projects/submit?userId=${state.currentUser.id}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.currentUser.token}`
            },
            body: JSON.stringify({ title, description, link })
        });
        
        if (!response.ok) throw new Error('Failed to submit project');
        
        await loadUserProjects();
        document.getElementById('projectForm').reset();
        showProjectForm();
        alert('Project submitted successfully!');
    } catch (error) {
        console.error('Error submitting project:', error);
        alert('Error submitting project');
    }
}

async function loadUserProjects() {
    try {
        const response = await fetch(`${API_BASE}/projects/user/${state.currentUser.id}`, {
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        if (!response.ok) throw new Error('Failed to load projects');
        const projects = await response.json();
        
        const list = document.getElementById('projectsList');
        list.innerHTML = '';
        
        if (projects.length === 0) {
            list.innerHTML = '<p class="message">No projects submitted yet.</p>';
            return;
        }
        
        projects.forEach(project => {
            const card = document.createElement('div');
            card.className = 'project-card';
            const score = project.assessmentScore ? `${project.assessmentScore}%` : 'Pending';
            
            card.innerHTML = `
                <h3>${project.title}</h3>
                <p>${project.description || 'No description'}</p>
                <a href="${project.link}" class="project-link" target="_blank">View Project →</a>
                <p><strong>Assessment:</strong> ${score}</p>
                <p>${project.assessmentNotes || ''}</p>
                <div class="project-actions">
                    <button onclick="deleteProject('${project.id}')" class="btn-secondary">Delete</button>
                </div>
            `;
            
            list.appendChild(card);
        });
    } catch (error) {
        console.error('Error loading projects:', error);
    }
}

async function deleteProject(projectId) {
    if (!confirm('Delete this project?')) return;
    
    try {
        await fetch(`${API_BASE}/projects/${projectId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${state.currentUser.token}` }
        });
        
        await loadUserProjects();
    } catch (error) {
        console.error('Error deleting project:', error);
    }
}

// ===== CHAT =====
async function sendChatMessage() {
    const input = document.getElementById('userMessage');
    const message = input.value.trim();
    
    if (!message) return;
    
    // Add user message to UI
    addMessageToChat(message, 'user');
    input.value = '';
    
    try {
        // In a real implementation, this would use the existing chat endpoints
        // For now, we'll send to the general chat
        const response = await fetch(`${API_BASE}/chat/send`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.currentUser.token}`
            },
            body: JSON.stringify({
                sessionId: state.sessionId || 'default',
                message: message,
                userId: state.currentUser.id
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            if (data.reply) {
                addMessageToChat(data.reply, 'assistant');
            }
        }
    } catch (error) {
        console.error('Error sending message:', error);
        addMessageToChat('Sorry, I encountered an error. Please try again.', 'assistant');
    }
}

function handleChatKeyPress(event) {
    if (event.key === 'Enter') {
        sendChatMessage();
    }
}

function addMessageToChat(text, role) {
    const messagesContainer = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${role}`;
    messageDiv.textContent = text;
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// ===== INITIALIZATION ON PAGE LOAD =====
document.addEventListener('DOMContentLoaded', () => {
    // Show home section by default
    showSection('home');
    
    // Load all user projects on page load
    loadUserProjects().catch(e => console.log('Projects not available yet'));
});
