#!/bin/bash

set -e

echo "🚀 Building SmartJARVIS project..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_dependencies() {
    print_status "Checking dependencies..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 21+"
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven"
        exit 1
    fi
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js 18+"
        exit 1
    fi
    
    # Check npm
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed. Please install npm"
        exit 1
    fi
    
    # Check Python
    if ! command -v python3 &> /dev/null; then
        print_error "Python 3 is not installed. Please install Python 3.11+"
        exit 1
    fi
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_warning "Docker is not installed. Docker builds will be skipped."
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_warning "Docker Compose is not installed. Docker builds will be skipped."
    fi
    
    print_status "All dependencies checked!"
}

# Build Java modules
build_java_modules() {
    print_status "Building Java modules..."
    
    # Build parent project
    cd "$(dirname "$0")/.."
    mvn clean install -DskipTests
    
    print_status "Java modules built successfully!"
}

# Build NLP Engine
build_nlp_engine() {
    print_status "Building NLP Engine..."
    
    cd "$(dirname "$0")/../nlp-engine"
    
    # Install dependencies
    npm install
    
    # Build TypeScript
    npm run build
    
    print_status "NLP Engine built successfully!"
}

# Setup Python environment
setup_python_env() {
    print_status "Setting up Python environment..."
    
    cd "$(dirname "$0")/../speech-service"
    
    # Create virtual environment if it doesn't exist
    if [ ! -d "venv" ]; then
        python3 -m venv venv
    fi
    
    # Activate virtual environment and install dependencies
    source venv/bin/activate
    pip install --upgrade pip
    pip install -r requirements.txt
    
    print_status "Python environment setup complete!"
}

# Build Docker images
build_docker_images() {
    if command -v docker &> /dev/null && command -v docker-compose &> /dev/null; then
        print_status "Building Docker images..."
        
        cd "$(dirname "$0")/../docker"
        docker-compose build
        
        print_status "Docker images built successfully!"
    else
        print_warning "Skipping Docker builds (Docker not available)"
    fi
}

# Main build process
main() {
    print_status "Starting SmartJARVIS build process..."
    
    # Store current directory
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
    
    # Change to project root
    cd "$PROJECT_ROOT"
    
    # Run build steps
    check_dependencies
    build_java_modules
    build_nlp_engine
    setup_python_env
    build_docker_images
    
    print_status "🎉 All components built successfully!"
    print_status "You can now run './scripts/start.sh' to start all services"
}

# Run main function
main "$@"
