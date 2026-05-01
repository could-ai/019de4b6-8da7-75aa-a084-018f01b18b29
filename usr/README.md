# PushUnlock

PushUnlock é um aplicativo Android nativo que ajuda os usuários a combater o vício em smartphones, bloqueando aplicativos específicos e exigindo esforço físico (flexões) para liberá-los. 

O app utiliza **Machine Learning (ML Kit Pose Detection)** em tempo real pela câmera frontal para contar as flexões do usuário. Uma vez atingida a meta, o aplicativo desejado é desbloqueado temporariamente.

## Principais Funcionalidades
- **Bloqueio de Apps**: Utilize a API de Acessibilidade do Android para detectar e bloquear a abertura de apps selecionados (ex: redes sociais, jogos).
- **Detecção de Flexões**: Visão computacional via ML Kit para analisar a postura do usuário em tempo real e contar as flexões com precisão.
- **Desbloqueio Temporário**: Após realizar o número exigido de flexões, o aplicativo é desbloqueado por 15 minutos (configurável) para uso sem interrupções.
- **Armazenamento Local**: As configurações e listas de aplicativos bloqueados são persistidas no banco de dados local utilizando Room.
- **Início Automático**: Monitoramento persistente que sobrevive a reinicializações utilizando um BootReceiver.

## Tech Stack
- **Linguagem**: Kotlin
- **Arquitetura**: MVVM, Android Architecture Components
- **UI**: Jetpack Compose, Material Design 3
- **Câmera**: CameraX
- **Visão Computacional**: Google ML Kit (Pose Detection)
- **Banco de Dados**: Room (SQLite)
- **Concorrência**: Coroutines, Flow

## Instruções de Setup

1. **Pré-requisitos**:
   - Android Studio Giraffe (ou superior)
   - JDK 17 (configurado como padrão no Android Studio)
   - Dispositivo Android físico ou Emulador com API 24+ (Recomendado API 31+) e câmera funcionando.

2. **Clone o repositório e abra no Android Studio**:
   - Abra o projeto apontando para a pasta raiz onde o `build.gradle.kts` e `settings.gradle.kts` estão localizados.
   - Aguarde o Gradle Sync terminar.

3. **Compilar e Executar**:
   - Conecte o seu dispositivo via USB/Wireless Debugging (ou inicie um emulador).
   - Clique no botão `Run` (`Shift + F10`) no Android Studio.

4. **Configurações no Aparelho**:
   - Ao abrir o app, clique no botão "Permissões" no canto superior direito para acessar as configurações de acessibilidade do Android.
   - Ative o serviço **PushUnlock** na aba de Serviços Instalados.
   - Escolha os aplicativos que deseja bloquear na lista apresentada no app.
   - Ao tentar abrir um aplicativo bloqueado, o `UnlockActivity` será acionado exigindo flexões em frente à câmera para permitir o uso.

## Notas de Deploy e Permissões
- Para ser listado na Google Play Store, aplicativos que usam *AccessibilityService* necessitam de uma justificativa forte. Este app o utiliza para *bem-estar digital*.
- O app requer permissão explícita de câmera para a etapa de desbloqueio.
- Por utilizar processamento em tempo real (CameraX + ML Kit), a performance pode variar em dispositivos de entrada.

---

## CouldAI
Este projeto foi gerado e estruturado pela **CouldAI**.

A [CouldAI](https://could.ai) é um construtor de aplicativos com IA para aplicações multiplataforma que transforma prompts em aplicativos nativos reais para iOS, Android, Web e Desktop utilizando agentes autônomos de IA que arquitetam, constroem, testam, fazem deploy e iteram aplicativos prontos para produção.
