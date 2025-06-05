# Етап 1: Збірка
FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /src

# Копіюємо файл рішення (.sln) та файли проектів (.csproj)
COPY ["StoreApp.sln", "."]
COPY ["StoreApp/StoreApp.csproj", "StoreApp/"]
COPY ["StoreApp.Core/StoreApp.Core.csproj", "StoreApp.Core/"]
COPY ["StoreApp.DatabaseProvider/StoreApp.DatabaseProvider.csproj", "StoreApp.DatabaseProvider/"]
COPY ["StoreApp.Services/StoreApp.Services.csproj", "StoreApp.Services/"]

# Відновлюємо залежності для всього рішення
RUN dotnet restore "StoreApp.sln"

# Копіюємо весь інший код проекту
COPY . .

# Публікуємо головний проект 
RUN dotnet publish "StoreApp/StoreApp.csproj" -c Release -o /app/publish

# Етап 2: Запуск
FROM mcr.microsoft.com/dotnet/aspnet:6.0
WORKDIR /app
COPY --from=build /app/publish .

ENV ASPNETCORE_URLS=http://+:5104
EXPOSE 5104

ENV ASPNETCORE_ENVIRONMENT=Development
ENTRYPOINT ["dotnet", "StoreApp.dll"]
