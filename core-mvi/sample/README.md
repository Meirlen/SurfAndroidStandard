[Главная страница репозитория](../docs/main.md)

[TOC]

# Core MVI Sample
Является демонстрацией возможностей [core-mvi](../core-mvi/) и содержит некоторые файлы для инициализации работы с фреймворком.

**`Внимание!` Модуль находится в стадии активной разработки!**

## Начало работы
1. Необходимо реализовать базовые сущности, используемые в этом подходе: 

    * Reactor
      
    * EventHub 
      
    * Middleware
    
    * RxBinder
    
    Либо скопировать из пакета /ui/base шаблонные классы и методы-расширения. 
   
1. Для того, чтобы DI корректно работал со всеми классами из шаблона, необходимо скопировать код `ReactScreenModule` в `ScreenModule` вашего проекта, либо дополнительно подключать `ReactScreenModule` к компоненту экрана. 