#!/bin/bash

# SmartJARVIS Build Script
# Builds all services in the correct order

set -e

echo "🚀 Starting SmartJARVIS build process..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_status "Checking requirements..."
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven first."
        exit 1
    fi
    
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js first."
        exit 1
    fi
    
    if ! command -v python3 &> /dev/null; then
        print_error "Python 3 is not installed. Please install Python 3 first."
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        print_warning "Docker is not installed. Docker builds will be skipped."
    fi
    
    print_success "Requirements check completed"
}

# Build Java services
build_java_services() {
    print_status "Building Java services..."
    
    # Build Task Service
    print_status "Building Task Service..."
    cd task-service
    mvn clean package -DskipTests
    print_success "Task Service built successfully"
    cd ..
    
    # Build Gateway Service
    print_status "Building Gateway Service..."
    cd gateway
    mvn clean package -DskipTests
    print_success "Gateway Service built successfully"
    cd ..
}

# Build Node.js services
build_node_services() {
    print_status "Building Node.js services..."
    
    # Build NLP Engine
    print_status "Building NLP Engine..."
    cd nlp-engine
    npm install
    npm run build
    print_success "NLP Engine built successfully"
    cd ..
}

# Build Python services
build_python_services() {
    print_status "Building Python services..."
    
    # Build Speech Service
    print_status "Building Speech Service..."
    cd speech-service
    pip3 install -r requirements.txt
    print_success "Speech Service dependencies installed"
    cd ..
}

# Build Docker images
build_docker_images() {
    if ! command -v docker &> /dev/null; then
        print_warning "Docker not available, skipping Docker builds"
        return
    fi
    
    print_status "Building Docker images..."
    
    # Build Task Service image
    print_status "Building Task Service Docker image..."
    docker build -f docker/Dockerfile.task -t smartjarvis-task-service .
    print_success "Task Service Docker image built"
    
    # Build Gateway Service image
    print_status "Building Gateway Service Docker image..."
    docker build -f docker/Dockerfile.gateway -t smartjarvis-gateway .
    print_success "Gateway Service Docker image built"
    
    # Build NLP Engine image
    print_status "Building NLP Engine Docker image..."
    docker build -f docker/Dockerfile.nlp -t smartjarvis-nlp-engine .
    print_success "NLP Engine Docker image built"
    
    # Build Speech Service image
    print_status "Building Speech Service Docker image..."
    cd speech-service
    docker build -t smartjarvis-speech-service .
    print_success "Speech Service Docker image built"
    cd ..
}

# Main build process
main() {
    print_status "Starting SmartJARVIS build process..."
    
    # Check requirements
    check_requirements
    
    # Build services
    build_java_services
    build_node_services
    build_python_services
    
    # Build Docker images
    build_docker_images
    
    print_success "🎉 All services built successfully!"
    print_status "You can now run the services using:"
    echo "  - Local: ./scripts/start.sh"
    echo "  - Docker: docker-compose up -d"
}

# Run main function
main "$@"
