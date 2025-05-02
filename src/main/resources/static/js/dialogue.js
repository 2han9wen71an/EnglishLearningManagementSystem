// 对话模拟脚本
jQuery(document).ready(function($) {
    // 获取DOM元素
    const scenarioId = document.getElementById('scenarioId').value;
    const chatContainer = document.getElementById('chatContainer');
    const promptBox = document.getElementById('promptBox');
    const currentPrompt = document.getElementById('currentPrompt');
    const userInput = document.getElementById('userInput');
    const sendBtn = document.getElementById('sendBtn');
    const resetBtn = document.getElementById('resetBtn');
    const voiceBtn = document.getElementById('voiceBtn');
    const toggleAiModeBtn = document.getElementById('toggleAiModeBtn');
    const aiModeIndicator = document.getElementById('aiModeIndicator');
    
    // 默认使用AI模式
    let useAiMode = true;
    
    // 滚动到聊天区域底部
    function scrollToBottom() {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }
    
    // 添加消息到聊天区域
    function addMessage(content, isUser) {
        const messageDiv = document.createElement('div');
        messageDiv.className = isUser ? 'message user-message' : 'message ai-message';
        messageDiv.innerHTML = `<span>${content}</span><div class="clearfix"></div>`;
        chatContainer.appendChild(messageDiv);
        scrollToBottom();
    }
    
    // 发送用户输入并获取AI回复
    function sendMessage() {
        const message = userInput.value.trim();
        if (message === '') return;
        
        // 添加用户消息到聊天区域
        addMessage(message, true);
        
        // 清空输入框
        userInput.value = '';
        
        // 禁用发送按钮，显示加载状态
        sendBtn.disabled = true;
        sendBtn.innerHTML = '<i class="fa fa-spinner fa-spin"></i>';
        
        // 发送到服务器并获取AI回复
        $.ajax({
            url: '/processDialogue',
            type: 'POST',
            data: {
                scenarioId: scenarioId,
                userInput: message
            },
            success: function(response) {
                // 添加AI回复到聊天区域
                addMessage(response.content, false);
                
                // 更新提示
                if (response.prompt) {
                    currentPrompt.textContent = response.prompt;
                } else {
                    // 对话结束
                    promptBox.innerHTML = '<strong>对话已完成！</strong> 您可以点击"重新开始"按钮重新进行对话。';
                }
                
                // 恢复发送按钮状态
                sendBtn.disabled = false;
                sendBtn.innerHTML = '发送';
            },
            error: function() {
                addMessage('抱歉，发生了错误。请稍后再试。', false);
                
                // 恢复发送按钮状态
                sendBtn.disabled = false;
                sendBtn.innerHTML = '发送';
            }
        });
    }
    
    // 重置对话
    function resetDialogue() {
        // 清空聊天区域
        chatContainer.innerHTML = '';
        
        // 禁用重置按钮，显示加载状态
        resetBtn.disabled = true;
        resetBtn.innerHTML = '<i class="fa fa-spinner fa-spin"></i> 重置中';
        
        // 重置对话状态
        $.ajax({
            url: '/resetDialogue',
            type: 'POST',
            data: {
                scenarioId: scenarioId
            },
            success: function(response) {
                // 添加初始AI消息
                addMessage(response.content, false);
                
                // 重置提示
                if (response.prompt) {
                    currentPrompt.textContent = response.prompt;
                }
                
                // 启用输入
                userInput.disabled = false;
                sendBtn.disabled = false;
                voiceBtn.disabled = false;
                
                // 恢复重置按钮状态
                resetBtn.disabled = false;
                resetBtn.innerHTML = '重新开始';
            },
            error: function() {
                addMessage('抱歉，重置对话时发生错误。请刷新页面重试。', false);
                
                // 恢复重置按钮状态
                resetBtn.disabled = false;
                resetBtn.innerHTML = '重新开始';
            }
        });
    }
    
    // 切换AI模式
    function toggleAiMode() {
        $.ajax({
            url: '/toggleAiMode',
            type: 'POST',
            success: function(response) {
                useAiMode = response.useAiMode;
                
                // 更新AI模式指示器
                aiModeIndicator.textContent = 'AI模式: ' + (useAiMode ? '开启' : '关闭');
                aiModeIndicator.className = 'badge ml-2 ' + (useAiMode ? 'badge-info' : 'badge-secondary');
                
                // 显示提示消息
                addMessage(response.message, false);
                
                // 重置对话
                resetDialogue();
            },
            error: function() {
                alert('切换AI模式失败，请重试。');
            }
        });
    }
    
    // 语音识别功能
    function startSpeechRecognition() {
        if (!('webkitSpeechRecognition' in window)) {
            alert('您的浏览器不支持语音识别功能。请使用Chrome浏览器。');
            return;
        }
        
        const recognition = new webkitSpeechRecognition();
        recognition.continuous = false;
        recognition.interimResults = false;
        recognition.lang = 'en-US'; // 设置为英语
        
        // 开始录音
        voiceBtn.classList.add('btn-success');
        voiceBtn.classList.remove('btn-danger');
        voiceBtn.innerHTML = '<i class="fa fa-microphone-slash"></i>';
        
        recognition.start();
        
        // 获取结果
        recognition.onresult = function(event) {
            const transcript = event.results[0][0].transcript;
            userInput.value = transcript;
        };
        
        // 结束录音
        recognition.onend = function() {
            voiceBtn.classList.add('btn-danger');
            voiceBtn.classList.remove('btn-success');
            voiceBtn.innerHTML = '<i class="fa fa-microphone"></i>';
        };
        
        // 错误处理
        recognition.onerror = function(event) {
            console.error('语音识别错误:', event.error);
            voiceBtn.classList.add('btn-danger');
            voiceBtn.classList.remove('btn-success');
            voiceBtn.innerHTML = '<i class="fa fa-microphone"></i>';
            alert('语音识别失败，请重试或使用文本输入。');
        };
    }
    
    // 事件监听
    sendBtn.addEventListener('click', sendMessage);
    resetBtn.addEventListener('click', resetDialogue);
    voiceBtn.addEventListener('click', startSpeechRecognition);
    toggleAiModeBtn.addEventListener('click', toggleAiMode);
    
    // 按Enter键发送消息
    userInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
    
    // 初始滚动到底部
    scrollToBottom();
});
