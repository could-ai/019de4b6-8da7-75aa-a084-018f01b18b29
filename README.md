# PushUnlock

O **PushUnlock** é um aplicativo nativo para Android focado em produtividade e saúde física. Ele bloqueia o acesso a aplicativos viciantes e exige que você realize flexões reais (detectadas via inteligência artificial e câmera do celular) para liberá-los. 

O acesso aos aplicativos escolhidos só é liberado temporariamente após você concluir a meta de flexões que configurou.

## 🚀 Funcionalidades

* **Lista de Aplicativos Instalados**: Selecione e gerencie os aplicativos que deseja bloquear.
* **Bloqueio com Serviço de Acessibilidade**: O PushUnlock intercepta automaticamente a abertura dos aplicativos bloqueados, garantindo que não sejam usados sem esforço.
* **Detecção de Exercícios por IA**: Usando a câmera frontal e o Google ML Kit Pose Detection, o aplicativo identifica com precisão seus movimentos e conta as flexões feitas.
* **Configurações Flexíveis**: Determine a quantidade de flexões necessárias e por quanto tempo o aplicativo será liberado (ex: 10 flexões para 15 minutos de uso).
* **Interface Moderna e Sombria**: Design em Jetpack Compose com tema escuro e detalhes em azul neon para facilitar o uso a qualquer momento do dia.

## 🛠️ Tecnologias Utilizadas

Este projeto foi construído do zero utilizando o ecossistema moderno de desenvolvimento Android Nativo:

* **Linguagem**: Kotlin
* **Interface (UI)**: Jetpack Compose (Material 3)
* **Gerenciamento de Estado**: ViewModel e Coroutines/Flow
* **Visão Computacional e IA**: Google ML Kit (Pose Detection) e CameraX
* **Persistência de Dados**: Room Database (SQLite)
* **Monitoramento de Uso**: Accessibility Service

## ⚙️ Como Executar o Projeto

1. Clone este repositório para a sua máquina local.
2. Abra o projeto no **Android Studio** (versão Hedgehog ou superior é recomendada).
3. O Gradle fará a sincronização automática e o download das dependências (Jetpack Compose, Room, ML Kit, CameraX).
4. Conecte seu dispositivo Android (com Android 8.0 Oreo - API 26 ou superior) usando um cabo USB e certifique-se de que a depuração USB esteja ativada, ou utilize um emulador (Aviso: emuladores não possuem câmera funcional nativamente a menos que você configure a webcam host para passar para o emulador).
5. Clique em **Run 'app'** no Android Studio.

**Permissões Necessárias no Dispositivo**:
* O aplicativo solicitará permissão para uso da Câmera (necessário para a detecção das flexões).
* É indispensável conceder as permissões de **Acessibilidade** nas configurações do Android. Há um atalho no próprio app para as configurações onde o PushUnlock Service deve ser ativado.

---

## Sobre o CouldAI

Este aplicativo foi gerado com o **[CouldAI](https://could.ai)**, uma plataforma de inteligência artificial para criação de aplicativos multiplataforma que transforma prompts simples em projetos reais e nativos (iOS, Android, Web e Desktop) utilizando agentes de IA autônomos que arquitetam, constroem, testam, implantam e iteram aplicações prontas para produção.