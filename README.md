# 🌌 SOPHIA - Assistente Pessoal com Inteligência Artificial

**SOPHIA** é uma assistente pessoal inteligente desenvolvida para Android com interface futurista em **Jetpack Compose**, interação bidirecional por voz (fala e escuta) em português do Brasil e cérebro potencializado pela **API Gemini**.

---

## ✨ Recursos do Aplicativo

1. **Interação Bidirecional por Voz (Speech & TTS):**
   - **Escuta Nativa (`SpeechRecognizer`):** Reconhecimento de fala em português (pt-BR) com permissão em tempo de execução e detecção de decibéis (RMS) em tempo real.
   - **Voz Suave Feminina (`TextToSpeech`):** Configurada para localizar voz feminina suave em português (pt-BR), com velocidade e tom calibrados para máxima naturalidade.
2. **Cérebro de IA com Persona SOPHIA:**
   - Persona calibrada via System Instructions: acolhedora, inteligente, ágil, educada e prestativa.
   - Respostas otimizadas para serem ouvidas com clareza (sem excesso de caracteres ou marcações confusas de markdown).
   - Suporte a histórico de conversas recente mantendo o contexto do diálogo.
3. **Interface Futurista & Orbe Quântico Animado:**
   - **Orbe Dinâmico:** Reage em tempo real com estados visuais:
     - 🌀 *Espera (Idle):* Respiração cósmica suave.
     - 🎙️ *Ouvindo (Listening):* Anéis e ondas de energia que se expandem conforme o volume da sua voz.
     - 🔮 *Pensando (Thinking):* Rotação de anéis e núcleo quântico em tons de ciano e violeta.
     - 🔊 *Falando (Speaking):* Ondas sonoras oscilantes no ritmo da fala da Sophia.
   - Balões de chat estilizados com degradês e marcação de horário.
   - Botão para ouvir novamente qualquer resposta ou interromper a fala instantaneamente.
4. **Configurações Integradas no App:**
   - Botão de engrenagem para inserir/alterar sua chave da API Gemini a qualquer momento.
   - Sliders para customizar o tom (pitch) e a velocidade (rate) da voz.
5. **Compilação Automática no GitHub Actions:**
   - Workflow `.github/workflows/build.yml` pré-configurado para compilar o APK instalável e disponibilizá-lo para download na aba **Actions**.

---

## 🚀 Como subir para o GitHub e gerar o APK

Siga estes passos simples para subir o projeto para o seu GitHub e gerar o APK:

### 1. Inicializar o repositório Git local
Abra o terminal (PowerShell ou Bash) na pasta do projeto:
```bash
cd C:\Users\chuta\.gemini\antigravity\scratch\SophiaAssistant
git init
git add .
git commit -m "feat: Sophia AI Assistant inicial com build GitHub Actions"
```

### 2. Criar o repositório no GitHub
1. Acesse [github.com/new](https://github.com/new) e crie um novo repositório (pode ser público ou privado), por exemplo com o nome `SophiaAssistant`.
2. Não marque para adicionar README ou .gitignore (já criamos tudo aqui).

### 3. Enviar o código para o GitHub
Execute os comandos indicados pelo GitHub (substituindo pelo seu usuário):
```bash
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/SophiaAssistant.git
git push -u origin main
```

### 4. Baixar o arquivo APK compilado
1. No seu repositório no GitHub, clique na aba **Actions** no topo.
2. Você verá o workflow **"Build Sophia APK"** em execução.
3. Ao finalizar (leva cerca de 2 a 3 minutos), clique na execução concluída.
4. Na seção **Artifacts** na parte inferior da página, você encontrará o arquivo zip com o instalável:
   - **`Sophia-Assistant-APK`** (contendo `Sophia-Assistant-v1.0.apk`).
5. Transfira para o seu celular Android e instale! *(Se o Android perguntar, permita "Instalar apps de fontes desconhecidas")*.

---

## 🔑 Como Obter a Chave da API Gemini (Gratuita)

1. Acesse o **[Google AI Studio](https://aistudio.google.com/)** com sua conta Google.
2. Clique em **"Get API key"** e em seguida em **"Create API key"**.
3. Copie a chave gerada.
4. No aplicativo Sophia no seu celular:
   - Toque no ícone de **engrenagem (⚙️)** no topo superior direito.
   - Cole sua chave no campo **Chave da API Gemini**.
   - Toque em **Salvar**.
5. Pronto! A Sophia já estará 100% conectada e pronta para conversar com você!

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin 1.9.24
- **UI:** Jetpack Compose + Material 3
- **Design System:** Deep Dark Sci-Fi / Futurista (Ciano Neon, Violeta e Rosa Quântico)
- **Áudio Nativo:** Android `SpeechRecognizer` e `TextToSpeech` (pt-BR)
- **Rede & IA:** OkHttp 4 + Google Gemini REST API (modelo `gemini-1.5-flash`)
- **CI/CD:** GitHub Actions com JDK 17 (Temurin) e Gradle 8.7
